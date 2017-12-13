package com.capitalone.dashboard.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deployment_data")
public class DeploymentMap {
	@Id
	private ObjectId id;
	private String deploymentId;
	private Deployment deployment;
	private boolean deployed;
	private String application;
	private String version;
	private boolean systemCheck;
	private String date;
	private String ivtStatus;
	private String bvtStatus;
	
	public String getIvtStatus() {
		return ivtStatus;
	}
	public void setIvtStatus(String ivtStatus) {
		this.ivtStatus = ivtStatus;
	}
	public String getBvtStatus() {
		return bvtStatus;
	}
	public void setBvtStatus(String bvtStatus) {
		this.bvtStatus = bvtStatus;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public boolean isSystemCheck() {
		return systemCheck;
	}
	public void setSystemCheck(boolean systemCheck) {
		this.systemCheck = systemCheck;
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
}
