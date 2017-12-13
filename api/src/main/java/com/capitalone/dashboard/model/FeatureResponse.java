package com.capitalone.dashboard.model;

import java.time.LocalDate;
import java.util.Map;

public class FeatureResponse {
private String team;
private UserStoryData userStoryData;
private DefectData defectData;
private Map<LocalDate,Integer> submittedDefectCountByDate;

public String getTeam() {
	return team;
}
public void setTeam(String team) {
	this.team = team;
}
public UserStoryData getUserStoryData() {
	return userStoryData;
}
public void setUserStoryData(UserStoryData userStoryData) {
	this.userStoryData = userStoryData;
}
public DefectData getDefectData() {
	return defectData;
}
public void setDefectData(DefectData defectData) {
	this.defectData = defectData;
}
public Map<LocalDate, Integer> getSubmittedDefectCountByDate() {
	return submittedDefectCountByDate;
}
public void setSubmittedDefectCountByDate(Map<LocalDate, Integer> submittedDefectCountByDate) {
	this.submittedDefectCountByDate = submittedDefectCountByDate;
}

}
