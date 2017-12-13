package com.capitalone.dashboard.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.capitalone.dashboard.misc.HygieiaException;
import com.capitalone.dashboard.model.AnsibleComponentStatus;
import com.capitalone.dashboard.model.Collector;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.model.Deployment;
import com.capitalone.dashboard.model.DeploymentMap;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.EnvironmentStatus;
import com.capitalone.dashboard.model.Ivt;
import com.capitalone.dashboard.model.Pod;
import com.capitalone.dashboard.model.PodVersionMap;
import com.capitalone.dashboard.model.deploy.DeployDataResponse;
import com.capitalone.dashboard.model.deploy.Environment;
import com.capitalone.dashboard.model.deploy.NewDeployableUnit;
import com.capitalone.dashboard.model.deploy.NewEnvironment;
import com.capitalone.dashboard.model.deploy.Server;
import com.capitalone.dashboard.repository.AnsibleComponentStatusRepository;
import com.capitalone.dashboard.repository.CollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.DeploymentMapRepository;
import com.capitalone.dashboard.repository.EnvironmentComponentRepository;
import com.capitalone.dashboard.repository.IvtRepository;
import com.capitalone.dashboard.repository.PodRepository;
import com.capitalone.dashboard.repository.PodVersionMapRepository;
import com.capitalone.dashboard.request.CollectorRequest;
import com.capitalone.dashboard.request.DeployDataCreateRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service
public class NewDeployServiceImpl implements NewDeployService {

	//private static final Logger LOGGER = LoggerFactory.getLogger(NewDeployServiceImpl.class);
	private final ComponentRepository componentRepository;
	private final DeploymentMapRepository deploymentMapRepository;
	private final EnvironmentComponentRepository environmentComponentRepository;
	private final CollectorRepository collectorRepository;
	private final CollectorService collectorService;
	private final PodRepository podRepository;
	private final PodVersionMapRepository podVersionMapRepository;
	private final IvtRepository ivtRepository;
	private final AnsibleComponentStatusRepository ansibleComponentStatusRepository;
	private static final Logger LOG = LoggerFactory.getLogger(NewDeployServiceImpl.class);

	@Autowired
	public NewDeployServiceImpl(ComponentRepository componentRepository,
			DeploymentMapRepository deploymentMapRepository,

			EnvironmentComponentRepository environmentComponentRepository,
			CollectorRepository collectorRepository ,
			CollectorService collectorService,
			PodRepository podRepository,
			PodVersionMapRepository podVersionMapRepository,
			IvtRepository ivtRepository,
			AnsibleComponentStatusRepository ansibleComponentStatusRepository
			) {
		this.componentRepository = componentRepository;
		this.deploymentMapRepository = deploymentMapRepository;
		this.environmentComponentRepository = environmentComponentRepository;
		this.collectorRepository = collectorRepository;
		this.collectorService = collectorService;
		this.podRepository = podRepository;
		this.podVersionMapRepository = podVersionMapRepository;
		this.ivtRepository = ivtRepository;
		this.ansibleComponentStatusRepository = ansibleComponentStatusRepository;
	}

	@Override
	public List<DeployDataResponse> getDeployStatus(long date) {

		LocalDate localDate = Instant.ofEpochMilli(date).atZone(TimeZone.getTimeZone("Asia/Calcutta").toZoneId()).toLocalDate();
		Iterable<DeploymentMap> deploymentMaps = deploymentMapRepository.findByDate(localDate.toString());
		List<DeploymentMap> maps = Lists.newArrayList(deploymentMaps);
		int counter;
		for(Pod pod : podRepository.findAll()){
			counter=0;
			for(DeploymentMap map : deploymentMaps){
				if(ansibleComponentStatusRepository.findByPodName(map.getDeployment().getPod().getPod())!=null){
				map.setAnsibleComponentStatusGood(ansibleComponentStatusRepository.findByPodName(map.getDeployment().getPod().getPod()).isSuccess());
				}
				List<Ivt> ivts = ivtRepository.findByPodAndDate(map.getDeployment().getPod().getPod(), localDate.toString());
				if(ivts.isEmpty()){
					map.setIvtPresent(false);
				}
				else{
					map.setIvtPresent(true);
				}
				map.setIvt(ivts);
				if(pod.getPod().equals(map.getDeployment().getPod().getPod()))
				{
					counter++;
				}

			}
			if(counter==0){
				DeploymentMap map = new DeploymentMap();
				Deployment deployment = new Deployment();
				deployment.setPod(pod);
				deployment.setDeploymentStatus("No Deployment");
				map.setSystemCheck("No Deployment");
				if(ansibleComponentStatusRepository.findByPodName(pod.getPod())!=null){
					LOG.info(ansibleComponentStatusRepository.findByPodName(pod.getPod()).getPodName());
					map.setAnsibleComponentStatusGood(ansibleComponentStatusRepository.findByPodName(pod.getPod()).isSuccess());
				}
				else
				{
					map.setAnsibleComponentStatusGood(false);
				}
				PodVersionMap podVersionMap = podVersionMapRepository.findByPod(pod.getPod());
				if(podVersionMap!=null){

					map.setApplication(podVersionMap.getRelease());
					map.setVersion(podVersionMap.getVersion());
				}
				map.setDeployed(false);
				map.setDeployment(deployment);
				maps.add(map);
			}

		}

		return createResponse(maps);
	}
	public List<DeployDataResponse> createResponse(List<DeploymentMap> maps){
		Map<String,List<DeploymentMap>> response = new HashMap<>();
		response.put("Others", new ArrayList<DeploymentMap>());
		for(DeploymentMap map : maps){

			if(response.containsKey(map.getApplication())){
				response.get(map.getApplication()).add(map);
			}
			else{

				if(map.getApplication()!=null){
					List<DeploymentMap> list = new ArrayList<>();
					list.add(map);
					response.put(map.getApplication(),list);
				}
				else{
					response.get("Others").add(map);
				}
			}
		}
		return createDeployDataResponse(response);

	}

	public List<DeployDataResponse> createDeployDataResponse(Map<String,List<DeploymentMap>> responseMap){
		List<DeployDataResponse> responseList = new ArrayList<>();
		for(String key : responseMap.keySet()){
			DeployDataResponse dataResponse = new DeployDataResponse();
			dataResponse.setRelease(key);
			dataResponse.setDeploymentMap(responseMap.get(key));
			responseList.add(dataResponse);
		}
		return responseList ;
	}


	@Override
	public DataResponse<List<NewEnvironment>> getDeployStatus(ObjectId componentId) {
		Component component = componentRepository.findOne(componentId);

		Collection<CollectorItem> cis = component.getCollectorItems()
				.get(CollectorType.Deployment);

		return getDeployStatus(cis);
	}
	@Override
	public Iterable<Pod> getPods(){
		return podRepository.findAll();
	}

	private DataResponse<List<NewEnvironment>> getDeployStatus(Collection<CollectorItem> deployCollectorItems) {
		List<NewEnvironment> environments = new ArrayList<>();
		long lastExecuted = 0;

		if (deployCollectorItems == null) {
			return new DataResponse<>(environments, 0);
		}

		// We will assume that if the component has multiple deployment collectors
		// then each collector will have a different url which means each Environment will be different
		for (CollectorItem item : deployCollectorItems) {
			String application = item.getDescription();

			List<DeploymentMap> components = deploymentMapRepository
					.findByApplicationAndDate(application,LocalDate.now().minusDays(7).toString(),new Sort(Sort.Direction.DESC, "date"));

			for ( DeploymentMap map : components) {
				AnsibleComponentStatus componentStatus;
				if(map.getDeployment().getPod().getPod().contains("WFN")){
					if(map.getDeployment().getPod().getPod().contains("WFNAUT01")){
						componentStatus = ansibleComponentStatusRepository.findByPodName("WFNAUTO01");
					}
					if(map.getDeployment().getPod().getPod().contains("WFNAUT02")){
						componentStatus = ansibleComponentStatusRepository.findByPodName("WFNAUTO02");
					}
					else{
						componentStatus = ansibleComponentStatusRepository.findByPodName(map.getDeployment().getPod().getPod());
					}
					if(componentStatus!=null){

						map.setAnsibleComponentStatusGood(componentStatus.isSuccess());
						map.setEnvironment(componentStatus.getEnvironmentName());
						map.setServerCount(componentStatus.getServerCount());
						map.setLastUpdated(componentStatus.getLastUpdated());
					}
					NewEnvironment env = new NewEnvironment(map.getDeployment().getPod().getPod(),map.getDeploymentId());
					environments.add(env);
					env.getNewUnits().add(
							new NewDeployableUnit(map));
				}

			}

			Collector collector = collectorRepository
					.findOne(item.getCollectorId());

			if (collector.getLastExecuted() > lastExecuted) {
				lastExecuted = collector.getLastExecuted();
			}
		}
		return new DataResponse<>(environments, lastExecuted);
	}

	@SuppressWarnings("unused")
	private Map<Environment, List<EnvironmentComponent>> groupByEnvironment(
			List<EnvironmentComponent> components) {
		Map<Environment, List<EnvironmentComponent>> map = new LinkedHashMap<>();
		for (EnvironmentComponent component : components) {
			Environment env = new Environment(component.getEnvironmentName(),
					component.getEnvironmentUrl());

			if (!map.containsKey(env)) {
				map.put(env, new ArrayList<EnvironmentComponent>());
			}

			// Following logic is to send only the latest deployment status - there may be better way to do this
			Iterator<EnvironmentComponent> alreadyAddedIter = map.get(env)
					.iterator();

			boolean found = false;
			ArrayList<EnvironmentComponent> toRemove = new ArrayList<EnvironmentComponent>();
			ArrayList<EnvironmentComponent> toAdd = new ArrayList<EnvironmentComponent>();
			while (alreadyAddedIter.hasNext()) {
				EnvironmentComponent ec = (EnvironmentComponent) alreadyAddedIter
						.next();
				if (component.getComponentName().equalsIgnoreCase(
						ec.getComponentName())) {
					found = true;
					if (component.getAsOfDate() > ec.getAsOfDate()) {
						toRemove.add(ec);
						toAdd.add(component);
					}
				}
			}
			if (!found) {
				toAdd.add(component);
			}
			map.get(env).removeAll(toRemove);
			map.get(env).addAll(toAdd);
		}

		return map;
	}

	@SuppressWarnings("unused")
	private Iterable<Server> servers(final EnvironmentComponent component,
			List<EnvironmentStatus> statuses) {
		return Iterables.transform(
				Iterables.filter(statuses, new ComponentMatches(component)),
				new ToServer());
	}

	private class ComponentMatches implements Predicate<EnvironmentStatus> {
		private EnvironmentComponent component;

		public ComponentMatches(EnvironmentComponent component) {
			this.component = component;
		}

		@Override
		public boolean apply(EnvironmentStatus environmentStatus) {
			return environmentStatus.getEnvironmentName().equals(
					component.getEnvironmentName())
					&& environmentStatus.getComponentName().equals(
							component.getComponentName());
		}
	}

	private class ToServer implements Function<EnvironmentStatus, Server> {
		@Override
		public Server apply(EnvironmentStatus status) {
			return new Server(status.getResourceName(), status.isOnline());
		}
	}

	@Override
	public String create(DeployDataCreateRequest request) throws HygieiaException {
		/**
		 * Step 1: create Collector if not there
		 * Step 2: create Collector item if not there
		 * Step 3: Insert build data if new. If existing, update it.
		 */
		Collector collector = createCollector();

		if (collector == null) {
			throw new HygieiaException("Failed creating Deploy collector.", HygieiaException.COLLECTOR_CREATE_ERROR);
		}

		CollectorItem collectorItem = createCollectorItem(collector, request);

		if (collectorItem == null) {
			throw new HygieiaException("Failed creating Deploy collector item.", HygieiaException.COLLECTOR_ITEM_CREATE_ERROR);
		}

		EnvironmentComponent deploy = createEnvComponent(collectorItem, request);

		if (deploy == null) {
			throw new HygieiaException("Failed inserting/updating Deployment information.", HygieiaException.ERROR_INSERTING_DATA);
		}

		return deploy.getId().toString();

	}


	private Collector createCollector() {
		CollectorRequest collectorReq = new CollectorRequest();
		collectorReq.setName("Jenkins");  //for now hardcode it.
		collectorReq.setCollectorType(CollectorType.Deployment);
		Collector col = collectorReq.toCollector();
		col.setEnabled(true);
		col.setOnline(true);
		col.setLastExecuted(System.currentTimeMillis());
		return collectorService.createCollector(col);
	}

	private CollectorItem createCollectorItem(Collector collector, DeployDataCreateRequest request) {
		CollectorItem tempCi = new CollectorItem();
		tempCi.setCollectorId(collector.getId());
		tempCi.setDescription(request.getAppName());
		tempCi.setPushed(true);
		tempCi.setLastUpdated(System.currentTimeMillis());
		tempCi.setNiceName(request.getNiceName());
		Map<String, Object> option = new HashMap<>();
		option.put("applicationName", request.getAppName());
		option.put("instanceUrl", request.getInstanceUrl());
		tempCi.getOptions().putAll(option);

		CollectorItem collectorItem = collectorService.createCollectorItem(tempCi);
		return collectorItem;
	}

	private EnvironmentComponent createEnvComponent(CollectorItem collectorItem, DeployDataCreateRequest request) {
		EnvironmentComponent deploy = environmentComponentRepository.
				findByUniqueKey(collectorItem.getId(), request.getArtifactName(), request.getArtifactName(), request.getEndTime());
		if ( deploy == null) {
			deploy = new EnvironmentComponent();
		}

		deploy.setAsOfDate(System.currentTimeMillis());
		deploy.setCollectorItemId(collectorItem.getId());
		deploy.setComponentID(request.getArtifactGroup());
		deploy.setComponentName(request.getArtifactName());
		deploy.setComponentVersion(request.getArtifactVersion());
		deploy.setEnvironmentName(request.getEnvName());
		deploy.setDeployTime(request.getEndTime());
		deploy.setDeployed("SUCCESS".equalsIgnoreCase(request.getDeployStatus()));

		return environmentComponentRepository.save(deploy); // Save = Update (if ID present) or Insert (if ID not there)
	}
}












