package com.capitalone.dashboard.model;

public class Milestone extends ScopeOwnerCollectorItem{
private String name;
private String targetProject;
private String targetDate;
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getTargetProject() {
	return targetProject;
}
public void setTargetProject(String targetProject) {
	this.targetProject = targetProject;
}
public String getTargetDate() {
	return targetDate;
}
public void setTargetDate(String targetDate) {
	this.targetDate = targetDate;
}
}
