package com.capitalone.dashboard.repository;

import java.util.List;

import com.capitalone.dashboard.model.Service;

public interface NewServiceRepository extends ServiceRepository {

	List<Service> findByName(String name);
}
