package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.Pod;

public interface PodRepository extends CrudRepository<Pod,ObjectId>{
List<Pod> findByPodName(String podName);
}
