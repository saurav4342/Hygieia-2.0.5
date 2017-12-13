package com.capitalone.dashboard.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.PodStatus;

public interface PodStatusRepository extends CrudRepository<PodStatus,ObjectId>{

}
