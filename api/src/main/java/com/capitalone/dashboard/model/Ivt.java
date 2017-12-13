package com.capitalone.dashboard.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Ivt {
private String pod;
private String testRunId;
@Id
private ObjectId id;
private String testSuiteName;
private List<Testcases> testCases;
private long timeStamp;
private String date;
public ObjectId getId() {
	return id;
}
public String getTestRunId() {
	return testRunId;
}
public void setTestRunId(String testRunId) {
	this.testRunId = testRunId;
}
public String getDate() {
	return date;
}
public void setDate(String date) {
	this.date = date;
}
public long getTimeStamp() {
	return timeStamp;
}
public void setTimeStamp(long timeStamp) {
	this.timeStamp = timeStamp;
}
public String getPod() {
	return pod;
}
public void setPod(String pod) {
	this.pod = pod;
}
public String getTestSuiteName() {
	return testSuiteName;
}
public void setTestSuiteName(String testSuiteName) {
	this.testSuiteName = testSuiteName;
}
public List<Testcases> getTestCases() {
	return testCases;
}
public void setTestCases(List<Testcases> testCases) {
	this.testCases = testCases;
}
}


