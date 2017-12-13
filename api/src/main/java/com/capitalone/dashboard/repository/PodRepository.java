package com.capitalone.dashboard.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.Pod;

public interface PodRepository extends CrudRepository<Pod,ObjectId>{

}
