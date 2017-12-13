package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.PodVersionMap;

public interface PodVersionMapRepository extends CrudRepository<PodVersionMap,ObjectId>{
    //@Query(value="{'pod' : ?0}")
	 List<PodVersionMap> findByApp(String app);
	 @Query(value="{ 'pod' : ?0 }")
	 PodVersionMap findByPod(String pod);
	 @Query(value="{ 'release' : ?0 }" ,fields="{'pod' : 1}")
	 List<PodVersionMap> findByRelease(String release);
}
