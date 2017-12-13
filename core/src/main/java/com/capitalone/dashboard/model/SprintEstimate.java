package com.capitalone.dashboard.model;

public class SprintEstimate {
	private int openEstimate;
	private int inProgressEstimate;
	private int completeEstimate;
	private int totalEstimate;
	private int userStoryCount;
	private int defectCount;
	private int featureCount;
	private int projectCount;
	/**
	 * @return the openEstimate
	 */
	public int getOpenEstimate() {
		return openEstimate;
	}
	/**
	 * @param openEstimate the openEstimate to set
	 */
	public void setOpenEstimate(int openEstimate) {
		this.openEstimate = openEstimate;
	}
	/**
	 * @return the inProgressEstimate
	 */
	public int getInProgressEstimate() {
		return inProgressEstimate;
	}
	/**
	 * @param inProgressEstimate the inProgressEstimate to set
	 */
	public void setInProgressEstimate(int inProgressEstimate) {
		this.inProgressEstimate = inProgressEstimate;
	}
	/**
	 * @return the completeEstimate
	 */
	public int getCompleteEstimate() {
		return completeEstimate;
	}
	/**
	 * @param completeEstimate the completeEstimate to set
	 */
	public void setCompleteEstimate(int completeEstimate) {
		this.completeEstimate = completeEstimate;
	}
	/**
	 * @return the totalEstimate
	 */
	public int getTotalEstimate() {
		return totalEstimate;
	}
	/**
	 * @param totalEstimate the totalEstimate to set
	 */
	public void setTotalEstimate(int totalEstimate) {
		this.totalEstimate = totalEstimate;
	}
	public int getUserStoryCount() {
		return userStoryCount;
	}
	public void setUserStoryCount(int userStoryCount) {
		this.userStoryCount = userStoryCount;
	}
	public int getDefectCount() {
		return defectCount;
	}
	public void setDefectCount(int defectCount) {
		this.defectCount = defectCount;
	}
	public int getFeatureCount() {
		return featureCount;
	}
	public void setFeatureCount(int featureCount) {
		this.featureCount = featureCount;
	}
	public int getProjectCount() {
		return projectCount;
	}
	public void setProjectCount(int projectCount) {
		this.projectCount = projectCount;
	}
}
