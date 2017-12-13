package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.DeploymentMap;


public interface DeploymentMapRepository extends CrudRepository<DeploymentMap,ObjectId>{
	 @Query(value="{'deploymentId': ?0}")
	List<DeploymentMap> findByDeploymentId(String deploymentId);
}


