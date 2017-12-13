package com.capitalone.dashboard.collector;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.datafactory.SentinelDataFactory;
import com.capitalone.dashboard.model.PodStatus;
import com.capitalone.dashboard.model.SentinelCollector;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.PodStatusRepository;
import com.capitalone.dashboard.repository.SentinelCollectorRepository;

@Component
public class SentinelCollectorTask extends CollectorTask<SentinelCollector>{
	private final Logger LOGGER = LoggerFactory.getLogger(SentinelCollectorTask.class);
    private SentinelSettings sentinelSettings;
    private SentinelCollectorRepository sentinelCollectorRepository;
    private SentinelDataFactory sentinelDataFactory;
    private PodStatusRepository podStatusRepository;
    
    @Autowired
    public SentinelCollectorTask(SentinelSettings sentinelSettings,
    		TaskScheduler taskScheduler,
    		SentinelCollectorRepository sentinelCollectorRepository,
    		SentinelDataFactory sentinelDataFactory,
    		PodStatusRepository podStatusRepository){
    	super(taskScheduler,"Sentinel Collector");
    	this.sentinelSettings = sentinelSettings;
    	this.sentinelCollectorRepository = sentinelCollectorRepository;	
    	this.sentinelDataFactory = sentinelDataFactory;
    	this.podStatusRepository = podStatusRepository;
    }

	@Override
	public void collect(SentinelCollector collector) {
		
		
		try {
			podStatusRepository.deleteAll();
			LOGGER.info("Collecting Pod server info...");
			List<PodStatus> podStatusList = sentinelDataFactory.getPodStatus();
			podStatusRepository.save(podStatusList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error", e);
		}
	}

	@Override
	public SentinelCollector getCollector() {
		return SentinelCollector.prototype();
	}

	@Override
	public BaseCollectorRepository<SentinelCollector> getCollectorRepository() {
		return sentinelCollectorRepository;
	}

	@Override
	public String getCron() {
		return sentinelSettings.getCron();
	}
}
