package com.capitalone.dashboard.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;

import com.capitalone.dashboard.model.Milestone;

public interface MilestoneRepository extends BaseCollectorItemRepository<Milestone>{

	List<Milestone> findByName(String name);

	@Query( value = " { 'options.teamId' : ?0 } ")
	Milestone findByTeamId(String teamId);
}
