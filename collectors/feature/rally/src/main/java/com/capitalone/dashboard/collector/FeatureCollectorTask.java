package com.capitalone.dashboard.collector;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.datafactory.rally.RallyDataFactory;
import com.capitalone.dashboard.misc.HygieiaException;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.FeatureCollector;
import com.capitalone.dashboard.model.Milestone;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.FeatureCollectorRepository;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.repository.MilestoneRepository;


/**
 * Collects {@link FeatureCollector} data from feature content source system.
 */
@Component
public class FeatureCollectorTask extends CollectorTask<FeatureCollector> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureCollectorTask.class);

  
    private final FeatureCollectorRepository featureCollectorRepository;
    private final FeatureSettings featureSettings;
    private final MilestoneRepository milestoneRepository;
	private final RallyDataFactory rallyDataFactory;
	private final FeatureRepository featureRepository;
   
    
    @Autowired
    public FeatureCollectorTask(TaskScheduler taskScheduler,
            FeatureCollectorRepository featureCollectorRepository, FeatureSettings featureSettings,
            MilestoneRepository milestoneRepository,RallyDataFactory rallyDataFactory,FeatureRepository featureRepository) throws HygieiaException
             {
        super(taskScheduler,"Rally");
        this.featureCollectorRepository = featureCollectorRepository;
       this.featureSettings=featureSettings;
       
       this.milestoneRepository = milestoneRepository;
		this.rallyDataFactory = rallyDataFactory;
		this.featureRepository = featureRepository;
    
    }

    /**
     * Accessor method for the collector prototype object
     */
    @Override
    public FeatureCollector getCollector() {
        return FeatureCollector.prototype();
    }

    /**
     * Accessor method for the collector repository
     */
    @Override
    public BaseCollectorRepository<FeatureCollector> getCollectorRepository() {
        return featureCollectorRepository;
    }

    /**
     * Accessor method for the current chronology setting, for the scheduler
     */
    @Override
    public String getCron() {
        return featureSettings.getCron();
    }

    /**
     * The collection action. This is the task which will run on a schedule to
     * gather data from the feature content source system and update the
     * repository with retrieved data.
     */
    @Override
    public void collect(FeatureCollector collector) {
       try {  
       LOGGER.info("Fetching Milestones...");
       updateMongoInfo(collector.getId(),featureSettings.getReleases());
       LOGGER.info("Completed");
    
       }
       catch(Exception e){
    	   LOGGER.info("Exception",e);
       }
    }

    public void updateMongoInfo(ObjectId collectorId,String[] releases) throws URISyntaxException, IOException{
		List<Milestone> milestones = new ArrayList<Milestone>();
		for(String release : releases){
			Milestone milestone = new Milestone();
			milestone.setDescription(release);
			milestone.setName(release);
			if(release.contains(".")){
				String [] teamId = release.split("\\.");
				milestone.setTeamId(teamId[0]+teamId[1]+teamId[2]);
			}
			else {
				milestone.setTeamId(release);
			}
			if(milestoneRepository.findByName(milestone.getName()).isEmpty()){
				milestone.setCollectorId(collectorId);
				milestoneRepository.save(milestone);
			}
			milestones.add(milestone);
		}
		featureRepository.deleteAll();
		List<Feature> features = rallyDataFactory.getFeaturesForMilestones(milestones);
		List<Feature> deferredDefects = rallyDataFactory.getDeferredDefectsForMilestones(milestones);
		features.addAll(deferredDefects);
		LOGGER.info(deferredDefects.size()+"count");
		featureRepository.save(features);
		/*	else{
				for(UserStory userStory : feature.getUserStories()){
					if(featureRepository.findUserStoryByUserStoryNumber(userStory.getStoryNumber()).isEmpty()){
						LOGGER.info("Empty");
						Feature existingFeature = featureRepository.findbySNumber(feature.getsNumber()).get(0);
						existingFeature.getUserStories().add(userStory);
					}
				}

			}*/
	}
}
