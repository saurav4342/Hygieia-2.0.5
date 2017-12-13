package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.Ivt;

public interface IvtRepository extends CrudRepository<Ivt,ObjectId>{
@Query(value="{ 'pod' : ?0 , 'date' : ?1 }")	
List<Ivt> findByPodAndDate(String pod,String date);

List<Ivt> findByTestRunId(String testRunId);
}
