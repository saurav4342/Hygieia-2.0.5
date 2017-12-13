package com.capitalone.dashboard.collector;


import com.capitalone.dashboard.model.Build;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.HudsonCollector;
import com.capitalone.dashboard.model.HudsonJob;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.BuildRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.HudsonCollectorRepository;
import com.capitalone.dashboard.repository.HudsonJobRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * CollectorTask that fetches Build information from Hudson
 */
@Component
public class HudsonCollectorTask extends CollectorTask<HudsonCollector> {
    @SuppressWarnings("PMD.UnusedPrivateField")
//    private static final Log LOG = LogFactory.getLog(HudsonCollectorTask.class);

    private final HudsonCollectorRepository hudsonCollectorRepository;
    private final HudsonJobRepository hudsonJobRepository;
    private final BuildRepository buildRepository;
    private final HudsonClient hudsonClient;
    private final HudsonSettings hudsonSettings;
    private final ComponentRepository dbComponentRepository;

    @Autowired
    public HudsonCollectorTask(TaskScheduler taskScheduler,
                               HudsonCollectorRepository hudsonCollectorRepository,
                               HudsonJobRepository hudsonJobRepository,
                               BuildRepository buildRepository, HudsonClient hudsonClient,
                               HudsonSettings hudsonSettings,
                               ComponentRepository dbComponentRepository) {
        super(taskScheduler, "Hudson");
        this.hudsonCollectorRepository = hudsonCollectorRepository;
        this.hudsonJobRepository = hudsonJobRepository;
        this.buildRepository = buildRepository;
        this.hudsonClient = hudsonClient;
        this.hudsonSettings = hudsonSettings;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public HudsonCollector getCollector() {
        return HudsonCollector.prototype(hudsonSettings.getServers(), hudsonSettings.getNiceNames());
    }

    @Override
    public BaseCollectorRepository<HudsonCollector> getCollectorRepository() {
        return hudsonCollectorRepository;
    }

    @Override
    public String getCron() {
        return hudsonSettings.getCron();
    }

    @Override
    public void collect(HudsonCollector collector) {
        long start = System.currentTimeMillis();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        List<HudsonJob> existingJobs = hudsonJobRepository.findByCollectorIdIn(udId);
        List<HudsonJob> activeJobs = new ArrayList<>();
        List<String> activeServers = new ArrayList<>();
        activeServers.addAll(collector.getBuildServers());

        clean(collector, existingJobs);

        for (String instanceUrl : collector.getBuildServers()) {
            logBanner(instanceUrl);
            try {
                Map<HudsonJob, Set<Build>> buildsByJob = hudsonClient
                        .getInstanceJobs(instanceUrl);
                log("Fetched jobs", start);
                activeJobs.addAll(buildsByJob.keySet());
                addNewJobs(buildsByJob.keySet(), existingJobs, collector);
                addNewBuilds(enabledJobs(collector, instanceUrl), buildsByJob);
                log("Finished", start);
            } catch (RestClientException rce) {
                activeServers.remove(instanceUrl); // since it was a rest exception, we will not delete this job  and wait for
                // rest exceptions to clear up at a later run.
                log("Error getting jobs for: " + instanceUrl, start);
            }
        }
        // Delete jobs that will be no longer collected because servers have moved etc.
        deleteUnwantedJobs(activeJobs, existingJobs, activeServers, collector);
    }

    /**
     * Clean up unused hudson/jenkins collector items
     *
     * @param collector    the {@link HudsonCollector}
     * @param existingJobs
     */

    private void clean(HudsonCollector collector, List<HudsonJob> existingJobs) {
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
                .findAll()) {

            if (CollectionUtils.isEmpty(comp.getCollectorItems())) continue;

            List<CollectorItem> itemList = comp.getCollectorItems().get(CollectorType.Build);

            if (CollectionUtils.isEmpty(itemList)) continue;

            for (CollectorItem ci : itemList) {
                if (collector.getId().equals(ci.getCollectorId())) {
                    uniqueIDs.add(ci.getId());
                }
            }
        }
        List<HudsonJob> stateChangeJobList = new ArrayList<>();
        for (HudsonJob job : existingJobs) {
            if ((job.isEnabled() && !uniqueIDs.contains(job.getId())) ||  // if it was enabled but not on a dashboard
                    (!job.isEnabled() && uniqueIDs.contains(job.getId()))) { // OR it was disabled and now on a dashboard
                job.setEnabled(uniqueIDs.contains(job.getId()));
                stateChangeJobList.add(job);
            }
        }
        if (!CollectionUtils.isEmpty(stateChangeJobList)) {
            hudsonJobRepository.save(stateChangeJobList);
        }
    }

    /**
     * Delete orphaned job collector items
     * @param activeJobs
     * @param existingJobs
     * @param activeServers
     * @param collector
     */
    private void deleteUnwantedJobs(List<HudsonJob> activeJobs, List<HudsonJob> existingJobs, List<String> activeServers, HudsonCollector collector) {

        List<HudsonJob> deleteJobList = new ArrayList<>();
        for (HudsonJob job : existingJobs) {
            if (job.isPushed()) continue; // build servers that push jobs will not be in active servers list by design

            // if we have a collector item for the job in repository but it's build server is not what we collect, remove it.
            if (!collector.getBuildServers().contains(job.getInstanceUrl())) {
                deleteJobList.add(job);
            }

            //if the collector id of the collector item for the job in the repo does not match with the collector ID, delete it.
            if (!job.getCollectorId().equals(collector.getId())) {
                deleteJobList.add(job);
            }

            // this is to handle jobs that have been deleted from build servers. Will get 404 if we don't delete them.
            if (activeServers.contains(job.getInstanceUrl()) && !activeJobs.contains(job)) {
                deleteJobList.add(job);
            }

        }
        if (!CollectionUtils.isEmpty(deleteJobList)) {
            hudsonJobRepository.delete(deleteJobList);
        }
    }

    /**
     * Iterates over the enabled build jobs and adds new builds to the database.
     *
     * @param enabledJobs list of enabled {@link HudsonJob}s
     * @param buildsByJob maps a {@link HudsonJob} to a set of {@link Build}s.
     */
    private void addNewBuilds(List<HudsonJob> enabledJobs,
                              Map<HudsonJob, Set<Build>> buildsByJob) {
        long start = System.currentTimeMillis();
        int count = 0;

        for (HudsonJob job : enabledJobs) {
            if (job.isPushed()) continue;
            for (Build buildSummary : nullSafe(buildsByJob.get(job))) {
                if (isNewBuild(job, buildSummary)) {
                    Build build = hudsonClient.getBuildDetails(buildSummary
                            .getBuildUrl(), job.getInstanceUrl());
                    if (build != null) {
                        build.setCollectorItemId(job.getId());
                        buildRepository.save(build);
                        count++;
                    }
                }
            }
        }
        log("New builds", start, count);
    }

    private Set<Build> nullSafe(Set<Build> builds) {
        return builds == null ? new HashSet<Build>() : builds;
    }

    /**
     * Adds new {@link HudsonJob}s to the database as disabled jobs.
     *
     * @param jobs         list of {@link HudsonJob}s
     * @param existingJobs
     * @param collector    the {@link HudsonCollector}
     */
    private void addNewJobs(Set<HudsonJob> jobs, List<HudsonJob> existingJobs, HudsonCollector collector) {
        long start = System.currentTimeMillis();
        int count = 0;

        List<HudsonJob> newJobs = new ArrayList<>();
        for (HudsonJob job : jobs) {
            HudsonJob existing = null;
            if (!CollectionUtils.isEmpty(existingJobs) && (existingJobs.contains(job))) {
                existing = existingJobs.get(existingJobs.indexOf(job));
            }

            String niceName = getNiceName(job, collector);
            if (existing == null) {
                job.setCollectorId(collector.getId());
                job.setEnabled(false); // Do not enable for collection. Will be enabled when added to dashboard
                job.setDescription(job.getJobName());
                if (StringUtils.isNotEmpty(niceName)) {
                    job.setNiceName(niceName);
                }
                newJobs.add(job);
                count++;
            } else if (StringUtils.isEmpty(existing.getNiceName()) && StringUtils.isNotEmpty(niceName)) {
                existing.setNiceName(niceName);
                hudsonJobRepository.save(existing);
            }
        }
        //save all in one shot
        if (!CollectionUtils.isEmpty(newJobs)) {
            hudsonJobRepository.save(newJobs);
        }
        log("New jobs", start, count);
    }

    private String getNiceName(HudsonJob job, HudsonCollector collector) {
        if (CollectionUtils.isEmpty(collector.getBuildServers())) return "";
        List<String> servers = collector.getBuildServers();
        List<String> niceNames = collector.getNiceNames();
        if (CollectionUtils.isEmpty(niceNames)) return "";
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).equalsIgnoreCase(job.getInstanceUrl()) && (niceNames.size() > i)) {
                return niceNames.get(i);
            }
        }
        return "";
    }

    private List<HudsonJob> enabledJobs(HudsonCollector collector,
                                        String instanceUrl) {
        return hudsonJobRepository.findEnabledJobs(collector.getId(),
                instanceUrl);
    }

    @SuppressWarnings("unused")
	private HudsonJob getExistingJob(HudsonCollector collector, HudsonJob job) {
        return hudsonJobRepository.findJob(collector.getId(),
                job.getInstanceUrl(), job.getJobName());
    }

    private boolean isNewBuild(HudsonJob job, Build build) {
        return buildRepository.findByCollectorItemIdAndNumber(job.getId(),
                build.getNumber()) == null;
    }
}
