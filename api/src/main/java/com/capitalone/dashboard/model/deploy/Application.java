package com.capitalone.dashboard.model.deploy;

import java.util.List;

import com.capitalone.dashboard.model.DeploymentMap;

public class Application {
private String name;
private List<DeploymentMap> deploymentMaps;

public List<DeploymentMap> getDeploymentMaps() {
	return deploymentMaps;
}

public void setDeploymentMaps(List<DeploymentMap> deploymentMaps) {
	this.deploymentMaps = deploymentMaps;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}
}
