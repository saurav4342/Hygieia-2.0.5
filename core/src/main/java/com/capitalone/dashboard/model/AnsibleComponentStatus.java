package com.capitalone.dashboard.model;

public class AnsibleComponentStatus extends BaseModel{
private String podName;
private boolean success;
private String lastUpdated;
private String serverCount;
private String environmentName;

public String getPodName() {
	return podName;
}
public void setPodName(String podName) {
	this.podName = podName;
}
public String getLastUpdated() {
	return lastUpdated;
}
public void setLastUpdated(String lastUpdated) {
	this.lastUpdated = lastUpdated;
}
public boolean isSuccess() {
	return success;
}
public void setSuccess(boolean success) {
	this.success = success;
}
public String getServerCount() {
	return serverCount;
}
public void setServerCount(String serverCount) {
	this.serverCount = serverCount;
}
public String getEnvironmentName() {
	return environmentName;
}
public void setEnvironmentName(String environmentName) {
	this.environmentName = environmentName;
}
}
