package com.capitalone.dashboard.datafactory.rally;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.collector.FeatureSettings;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.Milestone;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

@Component
public class RallyDataFactoryImpl implements RallyDataFactory {
	//private FeatureSettings featureSettings;
	//private RallyClient rallyClient;
	private RallyRestApi restApi;
	private List<String> traversedStories = new ArrayList<String>();
	@Autowired
	public RallyDataFactoryImpl(FeatureSettings featureSettings,RallyClient rallyClient) throws URISyntaxException{
		//this.featureSettings=featureSettings;
		//this.rallyClient = rallyClient;
		this.restApi = rallyClient.getRallyClient(featureSettings.getRallyBaseUri(), featureSettings.getRallyApiKey());
	}
	


public JsonArray getRallyApiQueryResponseAsJson(String queryRequestParam, Fetch fetch, QueryFilter queryFilter) throws URISyntaxException, IOException{
	//RallyRestApi restApi = getRallyClient();
	QueryRequest request = new QueryRequest(queryRequestParam);
	request.setFetch(fetch);
	request.setScopedDown(true);
	request.setQueryFilter(queryFilter);
	QueryResponse response = restApi.query(request);
	if(response.getResults()!=null){
		return response.getResults();
	}
	return null;
}

public List<Milestone> getMilestones() throws URISyntaxException, IOException{
	List<Milestone> milestones = new ArrayList<Milestone>();
		for(JsonElement result : getRallyApiQueryResponseAsJson("Milestones", new Fetch("Name","TargetDate","TargetProject","ObjectID","Projects"), new QueryFilter("TargetDate",">",LocalDateTime.now().toString()))){
			JsonObject milestoneJsonObject = result.getAsJsonObject();
			 if(milestoneJsonObject.get("Name").getAsString().contains("TLM")){
				 Milestone milestone = new Milestone();
				 milestone.setName(milestoneJsonObject.get("Name").getAsString());
				 milestone.setDescription(milestone.getName());
				 milestone.setTargetDate(milestoneJsonObject.get("TargetDate").getAsString());
				 milestone.setTeamId(milestoneJsonObject.get("ObjectID").getAsString());
				 milestone.setTargetProject(milestoneJsonObject.getAsJsonObject("Projects").get("Count").getAsString());
				 //milestone.setTargetProject(milestoneJsonObject.get("Project.Name").getAsString());
				 milestones.add(milestone);
			 }
		}
	return milestones;
}

public List<Feature> getFeaturesForMilestones(List<Milestone> milestones) throws URISyntaxException, IOException{
	List<Feature> features = new ArrayList<Feature>();
	traversedStories.clear();
	for(Milestone milestone : milestones){
			for(JsonElement result : getRallyApiQueryResponseAsJson("Defects", new Fetch("Name","FormattedID","ScheduleState","Owner","Project","ObjectID","Priority","State"), new QueryFilter("TargetBuild","contains",milestone.getName()))){
				JsonObject project = result.getAsJsonObject();
				 Feature feature = new Feature();
				 feature.setsName(project.get("Name").getAsString());
				 feature.setsNumber(project.get("FormattedID").getAsString());
				 if(project.get("ScheduleState")!=null){
				 feature.setsStatus(project.get("ScheduleState").getAsString());
				 }
				 feature.setsId(project.get("ObjectID").getAsString());
				 feature.setsTeamName(milestone.getName());
				 feature.setsTeamID(milestone.getTeamId());
				 feature.setsProjectName(project.getAsJsonObject("Project").get("Name").getAsString());
				 feature.setPriority(project.get("Priority").getAsString());
				 feature.setsState(project.get("State").getAsString());
				 if(feature.getsProjectName().contains("TLM") && !traversedStories.contains(feature.getsNumber())){
				 features.add(feature);
				 traversedStories.add(feature.getsNumber());
				 }
			}
			
			for(JsonElement result : getRallyApiQueryResponseAsJson("Artifacts", new Fetch("Name","FormattedID","ScheduleState","Owner","Project","ObjectID","Milestones"), new QueryFilter("Milestones.Name","contains",milestone.getName()))){
				JsonObject project = result.getAsJsonObject();
				    if(project.get("FormattedID").getAsString().charAt(0)=='F'){
				    	features.addAll(getUserStoriesForFeature(project.get("Name").getAsString(),milestone));
				    	Feature feature = new Feature();
				    	 feature.setsName(project.get("Name").getAsString());
						 feature.setsNumber(project.get("FormattedID").getAsString());
						 feature.setsId(project.get("ObjectID").getAsString());
						 feature.setsTeamName(milestone.getName());
						 if(milestone.getTeamId().equals("v16")){
						 feature.setsTeamID("1600");
						 }
						 else{
							 feature.setsTeamID(milestone.getTeamId());
						 }
						 feature.setsProjectName(project.getAsJsonObject("Project").get("Name").getAsString());
						 if(feature.getsProjectName().contains("TLM") && !traversedStories.contains(feature.getsNumber())){
						 features.add(feature);
						 traversedStories.add(feature.getsNumber());
						 }
				    }
				    else{
				    	Feature feature = new Feature();
						 feature.setsName(project.get("Name").getAsString());
						 feature.setsNumber(project.get("FormattedID").getAsString());
						 if(project.get("ScheduleState")!=null){
						 feature.setsStatus(project.get("ScheduleState").getAsString());
						 }
						 feature.setsId(project.get("ObjectID").getAsString());
						 feature.setsTeamName(milestone.getName());
						 if(milestone.getTeamId().equals("v16")){
							 feature.setsTeamID("1600");
							 }
						 else{
							 feature.setsTeamID(milestone.getTeamId());
						 }
						 feature.setsProjectName(project.getAsJsonObject("Project").get("Name").getAsString());
						 if(feature.getsProjectName().contains("TLM") && !traversedStories.contains(feature.getsNumber())){
						 features.add(feature);
						 traversedStories.add(feature.getsNumber());
				    }
				    }
				}
				 
				 
			}
		
	return features;
	}

public List<Feature> getUserStoriesForFeature(String featureName,Milestone milestone) throws URISyntaxException, IOException{
	List<Feature> userStories = new ArrayList<Feature>();
	for(JsonElement result : getRallyApiQueryResponseAsJson("HierarchicalRequirement", new Fetch("Name","FormattedID","ScheduleState","Owner","Iteration","ObjectID","Project"), new QueryFilter("Feature.Name","=",featureName)))
	{
		JsonObject story = result.getAsJsonObject();
		Feature userStory = new Feature();
		userStory.setsName(story.get("Name").getAsString());
		userStory.setsStatus(story.get("ScheduleState").getAsString());
		userStory.setsNumber(story.get("FormattedID").getAsString());
		 userStory.setsId(story.get("ObjectID").getAsString());
		 if(milestone.getTeamId().equals("v16")){
		 userStory.setsTeamID("1600");
		 }
		 else{
			 userStory.setsTeamID(milestone.getTeamId());
		 }
		 userStory.setsTeamName(milestone.getName());
		 userStory.setsProjectName(story.getAsJsonObject("Project").get("Name").getAsString());
//		if(story.getAsJsonObject("Iteration").get("Name")!=null){
//		userStory.setIteration(story.getAsJsonObject("Iteration").get("Name").getAsString());
//		}
		 if(userStory.getsProjectName().contains("TLM") && !traversedStories.contains(userStory.getsNumber())){
		userStories.add(userStory);
		traversedStories.add(userStory.getsNumber());
		 }
	}
	return userStories;
}

public List<Feature> getDeferredDefectsForMilestones(List<Milestone> milestones) throws URISyntaxException, IOException{
	List<Feature> deferredDefects = new ArrayList<Feature>();
	for(Milestone milestone:milestones ) {
		for(JsonElement result : getRallyApiQueryResponseAsJson("Defects", new Fetch("Name","FormattedID","ScheduleState","Owner","Project","ObjectID","Priority","State","FoundInBuild","TargetBuild","CreationDate"),
				new QueryFilter("FoundInBuild","contains",milestone.getName()).and(new QueryFilter("Project.Name","contains","TLM")))){
						
			JsonObject project = result.getAsJsonObject();
			 Feature feature = new Feature();
			 feature.setsName(project.get("Name").getAsString());
			 feature.setsNumber(project.get("FormattedID").getAsString());
			 if(project.get("ScheduleState")!=null){
			 feature.setsStatus(project.get("ScheduleState").getAsString());
			 }
			 feature.setsId(project.get("ObjectID").getAsString());
			 //feature.setsTeamName(milestone.getName());
			 //feature.setsTeamID();
			 feature.setsProjectName(project.getAsJsonObject("Project").get("Name").getAsString());
			 feature.setPriority(project.get("Priority").getAsString());
			 feature.setsState(project.get("State").getAsString());
			 feature.setFoundInBuild(milestone.getTeamId());
			 feature.setCreationDate(LocalDateTime.parse(project.get("CreationDate").getAsString(),DateTimeFormatter.ISO_DATE_TIME).toLocalDate());
			 if((project.get("TargetBuild").isJsonNull() || (!project.get("TargetBuild").getAsString().contains(milestone.getName())&&!project.get("TargetBuild").getAsString().contains(milestone.getName()+"GA")))&&!traversedStories.contains(feature.getsNumber())){
			    feature.setDeferred(true);
			 }
			 else {
				 feature.setDeferred(false);
			 }
			 deferredDefects.add(feature);
			 traversedStories.add(feature.getsNumber());
		}
	}
	return deferredDefects;
}

}

