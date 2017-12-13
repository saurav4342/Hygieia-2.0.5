package com.capitalone.dashboard.model;

public class UserStory {
private String name;
private String storyNumber;
private String state;
private String owner;
private String iteration;
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getStoryNumber() {
	return storyNumber;
}
public void setStoryNumber(String storyNumber) {
	this.storyNumber = storyNumber;
}
public String getState() {
	return state;
}
public void setState(String state) {
	this.state = state;
}
public String getOwner() {
	return owner;
}
public void setOwner(String owner) {
	this.owner = owner;
}
public String getIteration() {
	return iteration;
}
public void setIteration(String iteration) {
	this.iteration = iteration;
}
}
