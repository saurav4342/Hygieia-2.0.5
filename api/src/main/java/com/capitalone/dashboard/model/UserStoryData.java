package com.capitalone.dashboard.model;

import java.util.List;

public class UserStoryData {
	private int acceptedCount;
	private int backlogCount;
	private int completedCount;
	private int definedCount;
	private int inProgressCount;
	private int totalCount;
	private List<Feature> userStories;
	public int getAcceptedCount() {
		return acceptedCount;
	}
	public void setAcceptedCount(int acceptedCount) {
		this.acceptedCount = acceptedCount;
	}
	public int getBacklogCount() {
		return backlogCount;
	}
	public void setBacklogCount(int backlogCount) {
		this.backlogCount = backlogCount;
	}
	public int getCompletedCount() {
		return completedCount;
	}
	public void setCompletedCount(int completedCount) {
		this.completedCount = completedCount;
	}
	public int getDefinedCount() {
		return definedCount;
	}
	public void setDefinedCount(int definedCount) {
		this.definedCount = definedCount;
	}
	public int getInProgressCount() {
		return inProgressCount;
	}
	public void setInProgressCount(int inProgressCount) {
		this.inProgressCount = inProgressCount;
	}
	public List<Feature> getUserStories() {
		return userStories;
	}
	public void setUserStories(List<Feature> userStories) {
		this.userStories = userStories;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
}
