package com.capitalone.dashboard.model;

public class TestCase {
private String testCaseTitle;
private String testCaseId;
private String testResult;
private String testCaseDuration;
private String errorMessage;
private boolean success;
public boolean isSuccess() {
	return success;
}
public void setSuccess(boolean success) {
	this.success = success;
}
public String getTestCaseTitle() {
	return testCaseTitle;
}
public void setTestCaseTitle(String testCaseTitle) {
	this.testCaseTitle = testCaseTitle;
}
public String getTestCaseId() {
	return testCaseId;
}
public void setTestCaseId(String testCaseId) {
	this.testCaseId = testCaseId;
}
public String getTestResult() {
	return testResult;
}
public void setTestResult(String testResult) {
	this.testResult = testResult;
}
public String getTestCaseDuration() {
	return testCaseDuration;
}
public void setTestCaseDuration(String testCaseDuration) {
	this.testCaseDuration = testCaseDuration;
}
public String getErrorMessage() {
	return errorMessage;
}
public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
}

}
