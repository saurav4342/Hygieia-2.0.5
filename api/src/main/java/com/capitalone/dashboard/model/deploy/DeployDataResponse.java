package com.capitalone.dashboard.model.deploy;

import java.util.List;

import com.capitalone.dashboard.model.DeploymentMap;

public class DeployDataResponse {
private String release;
private List<DeploymentMap> deploymentMap;
public String getRelease() {
	return release;
}
public void setRelease(String release) {
	this.release = release;
}
public List<DeploymentMap> getDeploymentMap() {
	return deploymentMap;
}
public void setDeploymentMap(List<DeploymentMap> deploymentMap) {
	this.deploymentMap = deploymentMap;
}
}
