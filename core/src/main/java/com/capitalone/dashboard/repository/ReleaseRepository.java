package com.capitalone.dashboard.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.Release;

public interface ReleaseRepository extends CrudRepository<Release,String>{

	List<Release> findByName(String name);
}
