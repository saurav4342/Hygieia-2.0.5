package com.capitalone.dashboard.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Pod{
private String podName;
@Id
private ObjectId id;
public ObjectId getId() {
	return id;
}
public String getPod(){
	return podName;
}
public void setPod(String podName){
	this.podName=podName;
}
}
