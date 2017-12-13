package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.DeploymentMap;


public interface DeploymentMapRepository extends CrudRepository<DeploymentMap,ObjectId>{
    @Query(value="{'deployment.pod.podName': ?0}")
	List<DeploymentMap> findByPodName(String podName);
    
    
    @Query(value="{'application': ?0 , 'date': {$gte: ?1}}, $orderby:{ 'date' :1}")
    List<DeploymentMap> findByApplicationAndDate(String application, String date ,Sort sort);
    
    
    @Query(value="{'date': ?0}")
    List<DeploymentMap> findByDate(String date);
}


