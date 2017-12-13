package com.capitalone.dashboard.collector;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.datafactory.SeleniumTestDataFactory;
import com.capitalone.dashboard.model.SeleniumTestCollector;
import com.capitalone.dashboard.model.TestPod;
import com.capitalone.dashboard.model.TestResult;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.SeleniumTestCollectorRepository;
import com.capitalone.dashboard.repository.SeleniumTestJobRepository;
import com.capitalone.dashboard.repository.TestResultRepository;


@Component
public class SeleniumTestCollectorTask extends
        CollectorTask<SeleniumTestCollector> {

	private final SeleniumTestCollectorRepository seleniumTestCollectorRepository;
	private final SeleniumSettings seleniumSettings;
	private final SeleniumTestDataFactory seleniumTestDataFactory;
	private final SeleniumTestJobRepository seleniumTestJobRepository;
	private final TestResultRepository testResultRepository;
	@Autowired
	public SeleniumTestCollectorTask(
			TaskScheduler taskScheduler,
			SeleniumTestCollectorRepository seleniumTestCollectorRepository,
			SeleniumTestJobRepository seleniumTestJobRepository,
			SeleniumSettings seleniumSettings,
			SeleniumTestDataFactory seleniumTestDataFactory,
			TestResultRepository testResultRepository
			) {
		super(taskScheduler, "SeleniumCollector");
		this.seleniumTestCollectorRepository = seleniumTestCollectorRepository;
		this.seleniumTestJobRepository = seleniumTestJobRepository;
		this.seleniumSettings = seleniumSettings;
		this.seleniumTestDataFactory = seleniumTestDataFactory;
		this.testResultRepository = testResultRepository;
	}

    @Override
    public SeleniumTestCollector getCollector() {
        return SeleniumTestCollector
                .prototype(seleniumSettings.getServers());
    }

    @Override
    public BaseCollectorRepository<SeleniumTestCollector> getCollectorRepository() {
        return seleniumTestCollectorRepository;
    }

    @Override
    public String getCron() {
        return seleniumSettings.getCron();
    }

    @Override
    public void collect(SeleniumTestCollector collector) {
    	long start = System.currentTimeMillis();
    	 
        
        try {
        	log("fetching test results",start);
			List<TestResult> results = seleniumTestDataFactory.getTestResult();
			//log("fetching jobs");
			for(TestResult result:results){
				TestPod pod = new TestPod();
		        pod.setCollectorId(collector.getId());
		        pod.setEnabled(true);
		        pod.setDescription(result.getTargetEnvName());
		        pod.setPodName(result.getTargetEnvName());
		        if(seleniumTestJobRepository.findByDescription(pod.getPodName()).isEmpty()){ 
		        	seleniumTestJobRepository.save(pod);
		        }
			result.setCollectorItemId(pod.getId());
            result.setTimestamp(System.currentTimeMillis());
			result.setUrl("http");
			TestResult checkResult = testResultRepository.findByDescription(result.getDescription());
			
			if(checkResult!=null)
				{
				if(checkResult.getEndTime()<result.getEndTime()){
					testResultRepository.delete(testResultRepository.findByDescription(checkResult.getDescription()).getId());
				testResultRepository.save(result);
				}
			}
			else{
			testResultRepository.save(result);
			}
			
			}
			
        	    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log(e.getMessage());
		}
        
        log("Finished", start);
    }

    /**
     * Clean up unused hudson/jenkins collector items
     *
     * @param collector the collector
     */



   
}
