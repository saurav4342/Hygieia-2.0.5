package com.capitalone.dashboard.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.AnsibleComponentStatus;

public interface AnsibleComponentStatusRepository extends CrudRepository<AnsibleComponentStatus,ObjectId> {

AnsibleComponentStatus findByPodName(String pod);
}
