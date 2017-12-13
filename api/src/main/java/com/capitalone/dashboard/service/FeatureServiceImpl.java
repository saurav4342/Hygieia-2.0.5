package com.capitalone.dashboard.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.capitalone.dashboard.model.Collector;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.model.DefectData;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.FeatureResponse;
import com.capitalone.dashboard.model.QScopeOwner;
import com.capitalone.dashboard.model.SprintEstimate;
import com.capitalone.dashboard.model.UserStoryData;
import com.capitalone.dashboard.repository.CollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import com.mysema.query.BooleanBuilder;

/**
 * The feature service.
 * <p>
 * Features can currently belong to 2 sprint types: scrum and kanban. In order to be considered part of the sprint
 * the feature must not be deleted and must have an "active" sprint asset state if the sprint is set. The following
 * logic also applies:
 * <p>
 * A feature is part of a scrum sprint if any of the following are true:
 * <ol>
 * <li>the feature has a sprint set that has start <= now <= end and end < EOT (9999-12-31T59:59:59.999999)</li>
 * </ol>
 * <p>
 * A feature is part of a kanban sprint if any of the following are true:
 * <ol>
 * <li>the feature does not have a sprint set</li>
 * <li>the feature has a sprint set that does not have an end date</li>
 * <li>the feature has a sprint set that has an end date >= EOT (9999-12-31T59:59:59.999999)</li>
 * </ol>
 */
@Service
public class FeatureServiceImpl implements FeatureService {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureServiceImpl.class);
	private final ComponentRepository componentRepository;
	private final FeatureRepository featureRepository;
	private final CollectorRepository collectorRepository;
  
	/**
	 * Default autowired constructor for repositories
	 *
	 * @param componentRepository
	 *            Repository containing components used by the UI (populated by
	 *            UI)
	 * @param collectorRepository
	 *            Repository containing all registered collectors
	 * @param featureRepository
	 *            Repository containing all features
	 */
	@Autowired
	public FeatureServiceImpl(ComponentRepository componentRepository,
			CollectorRepository collectorRepository, FeatureRepository featureRepository
			) {
		this.componentRepository = componentRepository;
		this.featureRepository = featureRepository;
		this.collectorRepository = collectorRepository;
		
	}

	/**
	 * Retrieves a single story based on a back-end story number
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param storyNumber
	 *            A back-end story ID used by a source system
	 * @return A data response list of type Feature containing a single story
	 */
	@Override
	public DataResponse<List<Feature>> getStory(ObjectId componentId, String storyNumber) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);

		QScopeOwner team = new QScopeOwner("team");
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(team.collectorItemId.eq(item.getId()));

		// Get one story based on story number, based on component
		List<Feature> story = featureRepository.getStoryByNumber(storyNumber);
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(story, collector.getLastExecuted());
	}

	/**
	 * Retrieves all stories for a given team and their current sprint
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing all features for
	 *         the given team and current sprint
	 */
	@Override
	public DataResponse<List<Feature>> getRelevantStories(ObjectId componentId, String teamId,
			Optional<String> agileType) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);

		QScopeOwner team = new QScopeOwner("team");
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(team.collectorItemId.eq(item.getId()));

		// Get teamId first from available collector item, based on component
		List<Feature> relevantStories = getFeaturesForCurrentSprints(teamId, agileType.isPresent()? agileType.get() : null, false);

		Collector collector = collectorRepository.findOne(item.getCollectorId());

		return new DataResponse<>(relevantStories, collector.getLastExecuted());
	}

	/**
	 * Retrieves all unique super features and their total sub feature estimates
	 * for a given team and their current sprint
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the unique
	 *         features plus their sub features' estimates associated to the
	 *         current sprint and team
	 */
	@Override
	public DataResponse<List<FeatureResponse>> getFeatureEpicEstimates(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		/*if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}*/

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);
		
		List<Feature> relevantFeatureEstimates = featureRepository.findByActiveEndingSprints(teamId, getCurrentISODateTime());
		List<Feature> deferredDefects = featureRepository.findByFoundInBuild(teamId);
		relevantFeatureEstimates.addAll(deferredDefects);
		//LOGGER.info(deferredDefects.size()+"size");
		Map<String, List<Feature>> epicIDToEpicFeatureMap = new HashMap<>();
		if(agileType.equals("scrum")){
			
		}
		else{
			
		// epicID : epic information (in the form of a Feature object)
	
		for (Feature tempRs : relevantFeatureEstimates) {
			String team = tempRs.getsProjectName();
			
			if (StringUtils.isEmpty(team))
				continue;
//			if(!tempRs.getsStatus().equals("In-Progress")){
//				continue;
//			}
			List<Feature> featuresForTeam = epicIDToEpicFeatureMap.get(team);
			    if(featuresForTeam == null){
			    	List<Feature> newFeatureList = new ArrayList<Feature>();
			    	newFeatureList.add(tempRs);
			    	epicIDToEpicFeatureMap.put(team, newFeatureList);
			    }
			    else{
				featuresForTeam.add(tempRs);
				epicIDToEpicFeatureMap.put(team, featuresForTeam);
			    }
			// if estimateMetricType is hours accumulate time estimate in minutes for better precision ... divide by 60 later
			
			
//			feature.setsEstimate(String.valueOf(Integer.valueOf(feature.getsEstimate()) + estimate));
		}
		
		
//		if (isEstimateTime(estimateMetricType)) {
//			// time estimate is in minutes but we want to return in hours
//			for (Feature f : epicIDToEpicFeatureMap.values()) {
//				f.setsEstimate(String.valueOf(Integer.valueOf(f.getsEstimate()) / 60));
//			}
//		}
		}
		Collector collector = collectorRepository.findOne(item.getCollectorId());
	
		return new DataResponse<>(createFeatureResponse(epicIDToEpicFeatureMap), collector.getLastExecuted());
	}
	
	public List<FeatureResponse> createFeatureResponse(Map<String,List<Feature>> teamToFeatureMap){
		List<FeatureResponse> featureResponseList = new ArrayList<FeatureResponse>();
		Map<LocalDate,Integer> submittedDefectsCountByDate = new TreeMap<LocalDate,Integer>();
		
		for(String team: teamToFeatureMap.keySet()){
			FeatureResponse featureResponse = new FeatureResponse();
			featureResponse.setTeam(team);
			getFeatureCount(teamToFeatureMap.get(team),featureResponse,submittedDefectsCountByDate);
			featureResponseList.add(featureResponse);
		}
		return featureResponseList;
	}
	public void getFeatureCount(List<Feature> features,FeatureResponse featureResponse,Map<LocalDate,Integer> submittedDefectsCountByDate){
		
		int acceptedCount=0,inProgressCount=0,definedCount=0,completedCount=0,backlogCount=0;
		int closed = 0,open=0,fixed=0,submitted=0,p1=0,p2=0,p3=0,p4=0,noPriority=0,noState=0,dClosed=0,dOpen=0,dFixed=0,dSubmitted=0,dTotal=0,
				dp1=0,dp2=0,dp3=0,dp4=0,dpnone=0,dCount=0;
		List<Feature> userStories = new ArrayList<Feature>();
		List<Feature> defects = new ArrayList<Feature>();
		for(Feature feature : features){
			if(feature.getsStatus()!=null && feature.getsNumber().startsWith("US")){
				userStories.add(feature);
				
			switch(feature.getsStatus()){
			case "Defined":
				definedCount++;
				break;
			case "In-Progress":
				inProgressCount++;
				break;
			case "Accepted":
				acceptedCount++;
				break;
			case "Completed":
				completedCount++;
				break;
			case "Backlog":
				backlogCount++;
				break;
			default:
				break;
			}
			}
			else if(feature.getsNumber().startsWith("DE")&&feature.getsTeamID()!=null){
				defects.add(feature);
				if(feature.getsState()!=null){
				switch(feature.getsState()){
				case "Closed":
					closed++;
					break;
				case "Open":
					open++;
					break;
				case "Fixed":
				    fixed++;
				    break;
				case "Submitted":
					submitted++;
					break;
					default:
						break;
				}
				}
				else{
					noState++;
				}
				if(feature.getPriority()!=null){
				switch(feature.getPriority()){
				case "Normal":
					p3++;
					break;
				case "Low":
					p4++;
					break;
				case "None":
					noPriority++;
					break;
				case "Resolve Immediately":
					p1++;
					break;
				case "High Attention":
					p2++;
					break;
					default:
						break;
				}
				}
				else{
					noPriority++;
				}
			}
			else if(feature.getsNumber().startsWith("DE")&&feature.getFoundInBuild()!=null) {
				dTotal++;
				switch(feature.getsState()){
				case "Closed":
					dClosed++;
					break;
				case "Open":
					dOpen++;
					break;
				case "Fixed":
				    dFixed++;
				    break;
				case "Submitted":
					dSubmitted++;
					break;
					default:
						break;
				}
				if(feature.getPriority()!=null){
					switch(feature.getPriority()){
					case "Normal":
						dp3++;
						break;
					case "Low":
						dp4++;
						break;
					case "None":
						dpnone++;
						break;
					case "Resolve Immediately":
						dp1++;
						break;
					case "High Attention":
						dp2++;
						break;
						default:
							break;
					}
					}
			}
			 if(feature.getsNumber().startsWith("DE")&&feature.isDeferred()) {
				dCount++;
			}
			if(feature.getsNumber().startsWith("DE")&&feature.getFoundInBuild()!=null) {
			//	LOG.info("inside loop");
				int count=0;
				if(submittedDefectsCountByDate.containsKey(feature.getCreationDate()))
				{  
						count = submittedDefectsCountByDate.get(feature.getCreationDate());
						count++;
						submittedDefectsCountByDate.put(feature.getCreationDate(), count);
					}
				else {
					submittedDefectsCountByDate.put(feature.getCreationDate(), 1);
				}
						
					}
				
			}
		
		UserStoryData userStoryData = new UserStoryData();
		userStoryData.setAcceptedCount(acceptedCount);
		userStoryData.setBacklogCount(backlogCount);
		userStoryData.setCompletedCount(completedCount);
		userStoryData.setDefinedCount(definedCount);
		userStoryData.setInProgressCount(inProgressCount);
		userStoryData.setUserStories(userStories);
		userStoryData.setTotalCount(userStories.size());
		featureResponse.setUserStoryData(userStoryData);
		DefectData defectData = new DefectData();
		defectData.setDefects(defects);
		defectData.setClosed(closed);
		defectData.setFixed(fixed);
		defectData.setOpen(open);
		defectData.setSubmitted(submitted);
		defectData.setNoState(noState);
		defectData.setP1(p1);
		defectData.setP2(p2);
		defectData.setP3(p3);
		defectData.setP4(p4);
		defectData.setNoPriority(noPriority);
		defectData.setTotal(defects.size());
		defectData.setDeferredClosed(dClosed);
		defectData.setDeferredFixed(dFixed);
		defectData.setDeferredOpen(dOpen);
		defectData.setDeferredSubmitted(dSubmitted);
		defectData.setDeferredTotal(dTotal);
		defectData.setDp1(dp1);
		defectData.setDp2(dp2);
		defectData.setDp3(dp3);
		defectData.setDp4(dp4);
		defectData.setDpnone(dpnone);
		defectData.setdCount(dCount);
		featureResponse.setDefectData(defectData);
		featureResponse.setSubmittedDefectCountByDate(submittedDefectsCountByDate);
	}
	
	@Override
	public DataResponse<SprintEstimate> getAggregatedSprintEstimates(ObjectId componentId,
			String teamId, Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return new DataResponse<SprintEstimate>(new SprintEstimate(), 0);
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		
		SprintEstimate estimate = getSprintEstimates(teamId, agileType, estimateMetricType);
		return new DataResponse<>(estimate, collector.getLastExecuted());
	}

	/**
	 * Retrieves estimate total of all features in the current sprint and for
	 * the current team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the total
	 *         estimate number for all features
	 */
	@Override
	@Deprecated 
	public DataResponse<List<Feature>> getTotalEstimate(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);
		
		SprintEstimate estimate = getSprintEstimates(teamId, agileType, estimateMetricType);
		
		List<Feature> list = Collections.singletonList(new Feature());
		list.get(0).setsEstimate(Integer.toString(estimate.getTotalEstimate()));
		
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(list, collector.getLastExecuted());
	}

	/**
	 * Retrieves estimate in-progress of all features in the current sprint and
	 * for the current team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the in-progress
	 *         estimate number for all features
	 */
	@Override
	@Deprecated
	public DataResponse<List<Feature>> getInProgressEstimate(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);
		
		SprintEstimate estimate = getSprintEstimates(teamId, agileType, estimateMetricType);
		
		List<Feature> list = Collections.singletonList(new Feature());
		list.get(0).setsEstimate(Integer.toString(estimate.getInProgressEstimate()));
		
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(list, collector.getLastExecuted());
	}

	/**
	 * Retrieves estimate done of all features in the current sprint and for the
	 * current team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the done estimate
	 *         number for all features
	 */
	@Override
	@Deprecated
	public DataResponse<List<Feature>> getDoneEstimate(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);
		
		SprintEstimate estimate = getSprintEstimates(teamId, agileType, estimateMetricType);
		
		List<Feature> list = Collections.singletonList(new Feature());
		list.get(0).setsEstimate(Integer.toString(estimate.getCompleteEstimate()));
		
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(list, collector.getLastExecuted());
	}

	/**
	 * Retrieves the current sprint's detail for a given team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference
	 *            collector item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing several relevant
	 *         sprint fields for the current team's sprint
	 */
	@Override
	public DataResponse<List<Feature>> getCurrentSprintDetail(ObjectId componentId, String teamId,
			Optional<String> agileType) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils
						.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);

		// Get teamId first from available collector item, based on component
		List<Feature> sprintResponse = getFeaturesForCurrentSprints(teamId, agileType.isPresent()? agileType.get() : null, true);

		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(sprintResponse, collector.getLastExecuted());
	}
	
	private SprintEstimate getSprintEstimates(String teamId, Optional<String> agileType, Optional<String> estimateMetricType) {
		List<Feature> storyEstimates = getFeaturesForCurrentSprints(teamId, agileType.isPresent()? agileType.get() : null, true);

		int totalEstimate = 0;
		int wipEstimate = 0;
		int doneEstimate = 0;
		int userStoryCount=0;
		int featureCount=0;
		int defectCount=0;
		int projectCount=0;
		//LOGGER.info(storyEstimates.size()+" "+"size");
		List<String> projectsTraversed = new ArrayList<String>();
		for (Feature tempRs : storyEstimates) {
			String tempStatus = tempRs.getsStatus() != null? tempRs.getsStatus().toLowerCase() : null;
			// if estimateMetricType is hours accumulate time estimate in minutes for better precision ... divide by 60 later
			//int estimate = getEstimate(tempRs, estimateMetricType);
			//LOGGER.info(estimate+" ");
			if(!projectsTraversed.contains(tempRs.getsProjectName())){
				if(tempRs.getsProjectName().contains("TLM"))
				projectCount++;
				projectsTraversed.add(tempRs.getsProjectName());
			}
			totalEstimate ++;
			switch(tempRs.getsNumber().charAt(0)){
			case 'D':
				defectCount++;
				break;
			case 'F':
				featureCount++;
				break;
			case 'U':
			    userStoryCount++;
			    break;
			}
			if (tempStatus != null) {
				switch (tempStatus) {
					case "in-progress":
					//case "defined":
					case "blocked":
						wipEstimate ++;
					break;
					case "completed":
					case "accepted":
						doneEstimate ++;
					break;
				}
			}
		}
		

		int openEstimate = totalEstimate - wipEstimate - doneEstimate;
		
		if (isEstimateTime(estimateMetricType)) {
			// time estimate is in minutes but we want to return in hours
			totalEstimate /= 60;
			openEstimate /= 60;
			wipEstimate /= 60;
			doneEstimate /= 60;
		}
		
		SprintEstimate response = new SprintEstimate();
		response.setOpenEstimate(openEstimate);
		response.setInProgressEstimate(wipEstimate);
		response.setCompleteEstimate(doneEstimate);
		response.setTotalEstimate(totalEstimate);
        response.setDefectCount(defectCount);
        response.setFeatureCount(featureCount);
        response.setUserStoryCount(userStoryCount);
        response.setProjectCount(projectCount);
		return response;
	}
	
	/**
	 * Get the features that belong to the current sprints
	 * 
	 * @param teamId		the team id
	 * @param agileType		the agile type. Defaults to "scrum" if null
	 * @param minimal		if the resulting list of Features should be minimally populated (see queries for fields)
	 * @return
	 */
	private List<Feature> getFeaturesForCurrentSprints(String teamId, String agileType, boolean minimal) {
		List<Feature> rt = new ArrayList<Feature>();
		
		String now = getCurrentISODateTime();
		
		if ( FeatureCollectorConstants.SPRINT_KANBAN.equalsIgnoreCase(agileType)) {
			/* 
			 * A feature is part of a kanban sprint if any of the following are true:
			 *   - the feature does not have a sprint set
			 *   - the feature has a sprint set that does not have an end date
			 *   - the feature has a sprint set that has an end date >= EOT (9999-12-31T59:59:59.999999)
			 */
			if (minimal) {
				rt.addAll(featureRepository.findByNullSprintsMinimal(teamId));
				rt.addAll(featureRepository.findByUnendingSprintsMinimal(teamId));
			} else {
				rt.addAll(featureRepository.findByNullSprints(teamId));
				rt.addAll(featureRepository.findByUnendingSprints(teamId));
			}
		} else {
			// default to scrum
			/*
			 * A feature is part of a scrum sprint if any of the following are true:
			 *   - the feature has a sprint set that has start <= now <= end and end < EOT (9999-12-31T59:59:59.999999)
			 */
			if (minimal) {

				rt.addAll(featureRepository.findByActiveEndingSprintsMinimal(teamId, now));
			} else {
				rt.addAll(featureRepository.findByActiveEndingSprints(teamId, now));
			}
		}
		
		return rt;
	}

	private DataResponse<List<Feature>> getEmptyLegacyDataResponse() {
		Feature f = new Feature();
		List<Feature> l = new ArrayList<>();
		l.add(f);
		return new DataResponse<>(l, 0);
	}

	/**
	 * Retrieves the current system time stamp in ISO date time format. Because
	 * this is not using SimpleTimeFormat, this should be thread safe.
	 *
	 * @return A string representation of the current date time stamp in ISO
	 *         format from the current time zone
	 */
	private String getCurrentISODateTime() {
		return DatatypeConverter.printDateTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}
	
	private boolean isEstimateTime(Optional<String> estimateMetricType) {
		return estimateMetricType.isPresent() && FeatureCollectorConstants.STORY_HOURS_ESTIMATE.equalsIgnoreCase(estimateMetricType.get());
	}
	
	private int getEstimate(Feature feature, Optional<String> estimateMetricType) {
		int rt = 0;
		
		if (isEstimateTime(estimateMetricType)) {
			if (feature.getsEstimateTime() != null) {
				rt = feature.getsEstimateTime().intValue();
			}
		} else {
			// default to story points since that should be the most common use case
			if (!StringUtils.isEmpty(feature.getsEstimate())) {
				rt = (int)Double.parseDouble(feature.getsEstimate());
			}
		}
		
		return rt;
	}
}