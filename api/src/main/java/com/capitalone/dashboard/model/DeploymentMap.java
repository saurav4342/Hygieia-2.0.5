package com.capitalone.dashboard.model;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deployment_data")
public class DeploymentMap {
	@Id
	private ObjectId id;
	private List<Ivt> ivts;
	private boolean ivtPresent; 
	private String deploymentId;
	private Deployment deployment;
	private boolean deployed;
	private String application;
	private String version;
	private String date;
	private boolean ansibleComponentStatusGood;
	private String systemCheck;
	private String serverCount;
	private String environment;
	private String lastUpdated;
	
	public boolean isIvtPresent() {
		return ivtPresent;
	}
	public void setIvtPresent(boolean ivtPresent) {
		this.ivtPresent = ivtPresent;
	}
	public List<Ivt> getIvt() {
		return ivts;
	}
	public void setIvt(List<Ivt> ivts) {
		this.ivts = ivts;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public ObjectId getId(){
		return id;
	}
	public void setDeploymentId(String deploymentId){
		this.deploymentId= deploymentId;
	}
	public void setDeployment(Deployment deployment){
		this.deployment=deployment;
	}
	public String getDeploymentId(){
		return deploymentId;
	}
	public Deployment getDeployment(){
		return deployment;
	}
	public boolean isDeployed(){
		return deployed;
	}
	public void setDeployed(boolean deployed){
		this.deployed = deployed;
	}
	public String getApplication(){
		return application;
	}
	public void setApplication(String application){
		this.application = application;
	}
	public String getVersion(){
		return version;
	}
	public void setVersion(String version){
		this.version = version;
	}
	public boolean isAnsibleComponentStatusGood() {
		return ansibleComponentStatusGood;
	}
	public void setAnsibleComponentStatusGood(boolean ansibleComponentStatus) {
		this.ansibleComponentStatusGood = ansibleComponentStatus;
	}
	public String getSystemCheck() {
		return systemCheck;
	}
	public void setSystemCheck(String systemCheck) {
		this.systemCheck = systemCheck;
	}
	public List<Ivt> getIvts() {
		return ivts;
	}
	public void setIvts(List<Ivt> ivts) {
		this.ivts = ivts;
	}
	public String getServerCount() {
		return serverCount;
	}
	public void setServerCount(String serverCount) {
		this.serverCount = serverCount;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
}
