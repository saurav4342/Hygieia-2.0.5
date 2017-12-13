package com.capitalone.dashboard.collector;

//import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.builder.RepositoryBuilder;
import com.capitalone.dashboard.datafactory.DeploymentDataFactory;
import com.capitalone.dashboard.model.AnsibleComponentStatus;
import com.capitalone.dashboard.model.BladeCollector;
import com.capitalone.dashboard.model.DeploymentMap;
import com.capitalone.dashboard.model.DeploymentTask;
import com.capitalone.dashboard.model.Ivt;
import com.capitalone.dashboard.model.PodVersionMap;
import com.capitalone.dashboard.model.SystemCheckResult;
import com.capitalone.dashboard.model.UDeployApplication;
import com.capitalone.dashboard.repository.BaseCollectorRepository;

@Component
public class BladeCollectorTask extends CollectorTask<BladeCollector>{
	 private static final Logger LOGGER = LoggerFactory.getLogger(BladeCollectorTask.class);
     private final BladeSettings bladeSettings;
     private final DeploymentDataFactory dataFactory;
     private final RepositoryBuilder repositoryBuilder;
     
     @Autowired
	 public BladeCollectorTask(TaskScheduler taskScheduler,
			                   BladeSettings bladeSettings,
			                   DeploymentDataFactory dataFactory,
			                   RepositoryBuilder repositoryBuilder
			                   ){
		 super(taskScheduler,"Blade");
	     this.bladeSettings = bladeSettings;
	     this.dataFactory = dataFactory;
	     this.repositoryBuilder = repositoryBuilder;
     }
	 
	    @Override
	    public BladeCollector getCollector() {
	        return BladeCollector.prototype();
	    }

	    @Override
	    public BaseCollectorRepository<BladeCollector> getCollectorRepository() {
	        return repositoryBuilder.getBladeCollectorRepository();
	    }

	    @Override
	    public String getCron() {
	        return bladeSettings.getCron();
	    }

	    @Override
	    public void collect(BladeCollector collector) {
	    	LOGGER.info("STARTING");
	    	
	    	try{
	    		LOGGER.info("Getting Data from Splunk..");
	    		List<DeploymentTask> taskList = dataFactory.connectToSplunk();
	    		LOGGER.info("Getting Version Data");
	    		List<PodVersionMap> podVersionMapList = dataFactory.getVersionData();
	    		LOGGER.info("Saving");
	    		repositoryBuilder.getPodVersionMapRepository().deleteAll();
	    		repositoryBuilder.getPodVersionMapRepository().save(podVersionMapList);
	    		LOGGER.info("Creating Map");
	    		List<SystemCheckResult> results = dataFactory.getSystemCheckResults();
	    		List<DeploymentMap> mapList = dataFactory.getDeploymentMap(taskList,podVersionMapList,results);
	    		List<UDeployApplication> podList = dataFactory.createPods(mapList);
	    		for(UDeployApplication pod:podList){
	    			if(repositoryBuilder.getuDeployApplicationRepository().findByDescription(pod.getDescription()).isEmpty()){
	    				pod.setCollectorId(collector.getId());
	    				pod.setInstanceUrl(pod.getApplicationName());
	    				repositoryBuilder.getuDeployApplicationRepository().save(pod);
	    			}
	    		}
	    		LOGGER.info("Saving..");
	    		for(DeploymentMap map : mapList){
	    			if(repositoryBuilder.getDeploymentMapRepository().findByDeploymentId(map.getDeploymentId()).isEmpty()){
	    				repositoryBuilder.getDeploymentMapRepository().save(map);
	    			}
	    			else{
	    				
	    				DeploymentMap check = repositoryBuilder.getDeploymentMapRepository().findByDeploymentId(map.getDeploymentId()).get(0);
	    				if(check.isSystemCheck()!=map.isSystemCheck()){
	    					check.setSystemCheck(map.isSystemCheck());
	    				}
	    				if(check.getVersion()!=map.getVersion()){
	    					check.setVersion(map.getVersion());
	    					check.setApplication(map.getApplication());
	    				}
	    				repositoryBuilder.getDeploymentMapRepository().save(check);
	    			}
	    			if(repositoryBuilder.getPodRepository().findByPodName(map.getDeployment().getPod().getPod()).isEmpty()){
	    				repositoryBuilder.getPodRepository().save(map.getDeployment().getPod());
	    			}
	    		}
	    		LOGGER.info("Getting IVT Results");
	    		List<Ivt> ivtList = dataFactory.getIvtResults();
	    		for(Ivt ivt : ivtList){
	    			ivt.getTestCases().remove(0);
	    			if(repositoryBuilder.getIvtRepository().findByTestRunId(ivt.getTestRunId()).isEmpty()){
	    				LOGGER.info(ivt.getTestSuiteName());
	    				repositoryBuilder.getIvtRepository().save(ivt);
	    				
	    			}
	    		}
	    		LOGGER.info("Getting ansible component status");
	    		List<AnsibleComponentStatus> ansibleComponentStatusComponents = dataFactory.getAnsibleComponentStatusList();
	    		repositoryBuilder.getAnsibleComponentStatusRepository().deleteAll();
	    		repositoryBuilder.getAnsibleComponentStatusRepository().save(ansibleComponentStatusComponents);
	    		LOGGER.info("Completed.");

	    	}
	    	catch(Exception e){
	    		LOGGER.error("error", e);
	    		LOGGER.info("Error");
	    	}
	    }
}
