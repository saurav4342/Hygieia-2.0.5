package com.capitalone.dashboard.model;

public class SystemCheckResult {
private String hostCount;
private String pod;
private boolean isGood;
public String getHostCount() {
	return hostCount;
}
public void setHostCount(String hostCount) {
	this.hostCount = hostCount;
}
public String getPod() {
	return pod;
}
public void setPod(String pod) {
	this.pod = pod;
}
public boolean isGood() {
	return isGood;
}
public void setGood(boolean isGood) {
	this.isGood = isGood;
}
}
