package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import com.capitalone.dashboard.model.TestPod;

/**
 *
 */
public interface SeleniumTestJobRepository extends BaseCollectorItemRepository<TestPod> {

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<TestPod> findEnabledJenkinsJobs(ObjectId collectorId, String instanceUrl);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.jobName : ?2}")
    TestPod findJenkinsJob(ObjectId collectorId, String instanceUrl, String jobName);
    
    @Query(value="{ 'description' : ?0}")
    TestPod findJenkinsJobByDescription(String description);
    
    @Query(value="{ 'podName' : ?0 }")
    List<TestPod> findByPodName(String podName);
    
    @Query(value="{ 'description' : ?0 }")
    List<TestPod> findByDescription(String description);
}
