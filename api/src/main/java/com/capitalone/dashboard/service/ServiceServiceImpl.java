package com.capitalone.dashboard.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.capitalone.dashboard.model.Dashboard;
import com.capitalone.dashboard.model.PodVersionMap;
import com.capitalone.dashboard.model.Service;
import com.capitalone.dashboard.model.ServiceStatus;
import com.capitalone.dashboard.model.SplunkEvent;
import com.capitalone.dashboard.repository.DashboardRepository;
import com.capitalone.dashboard.repository.NewServiceRepository;
import com.capitalone.dashboard.repository.PodVersionMapRepository;
//import com.capitalone.dashboard.repository.ServiceRepository;
import com.capitalone.dashboard.repository.SplunkEventRepository;


@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
	
    private final NewServiceRepository serviceRepository;
    private final DashboardRepository dashboardRepository;
    private final SplunkEventRepository eventRepository;
    private final PodVersionMapRepository podVersionMapRepository;
    @Autowired
    public ServiceServiceImpl(NewServiceRepository serviceRepository,
    		DashboardRepository dashboardRepository,
    		SplunkEventRepository eventRepository,
    		PodVersionMapRepository podVersionMapRepository) {
        this.serviceRepository = serviceRepository;
        this.dashboardRepository = dashboardRepository;
        this.eventRepository = eventRepository;
        this.podVersionMapRepository = podVersionMapRepository;
    }

    @Override
    public Iterable<SplunkEvent> all() {
        return eventRepository.findAll();
    }

    @Override
    public List<SplunkEvent> dashboardServices(ObjectId dashboardId) {
    	List<Service> serviceList = serviceRepository.findByDashboardId(dashboardId);
    	List<String> blankList = new ArrayList<String>();
    	List<SplunkEvent> eventList = new ArrayList<SplunkEvent>();
    	for(Service service : serviceList){
    		List<PodVersionMap> pods = podVersionMapRepository.findByRelease(service.getName());
    		for(PodVersionMap podVersionMap : pods){
    		SplunkEvent event = new SplunkEvent();
    		if(eventRepository.findByPod(podVersionMap.getPod())!=null){
    		event = eventRepository.findByPod(podVersionMap.getPod());
    		}
    		else{
    			event.setPod(podVersionMap.getPod());
    			event.setCustIDs(blankList);
    			event.setErrorCount("0");
    			event.setApp("");
    			event.setErrorMessages(blankList);
    			event.setPercentage("0");
    			event.setSyscodes(blankList);
    			event.setUCIdCount(0);
    			event.setuSysCodeCount(0);
    			event.setId(service.getId());
    		}
    		if(event.getPod().contains("WFN")){
    		eventList.add(event);
    		}
    	}
    	}
    	return eventList;
    }

    @Override
   public List<Service> dashboardDependentServices(ObjectId dashboardId) {
       return serviceRepository.findByDependedBy(dashboardId);
    }

    @Override
    public Service get(ObjectId id) {
        return serviceRepository.findOne(id);
    }

    @Override
    public Service create(ObjectId dashboardId, String name) {
        Service service = new Service();
        service.setName(name);
        service.setDashboardId(dashboardId);
        service.setStatus(ServiceStatus.Ok);
        service.setLastUpdated(System.currentTimeMillis());

        Dashboard dashboard = dashboardRepository.findOne(dashboardId);
        service.setApplicationName(dashboard.getApplication().getName());

        return serviceRepository.save(service);
    }

    @Override
    public Service update(ObjectId dashboardId, Service service) {
        if (!service.getDashboardId().equals(dashboardId)) {
            throw new IllegalStateException("Not allowed to update this service from this dashboard!");
        }
        service.setLastUpdated(System.currentTimeMillis());
        return serviceRepository.save(service);
    }

    @Override
    public void delete(ObjectId dashboardId, ObjectId serviceId) {
    	SplunkEvent splunkEvent = null;
    	if(getSplunkEvent(serviceId)!=null)
    	{
         splunkEvent = getSplunkEvent(serviceId);
         String pod = splunkEvent.getPod();
         for(Service service : serviceRepository.findByName(pod)){
         if (!service.getDashboardId().equals(dashboardId)) {
             continue;
         }
         else{
         serviceRepository.delete(service);
         }
        }
    	}
    	else{
    		serviceRepository.delete(serviceId);
    	}
    	
        
    }

    private SplunkEvent getSplunkEvent(ObjectId serviceId) {
    	SplunkEvent splunkEvent = eventRepository.findOne(serviceId);
    	return splunkEvent;
		// TODO Auto-generated method stub
		
	}

	@Override
    public Service addDependentService(ObjectId dashboardId, ObjectId serviceId) {
        Service service = get(serviceId);
        if (service.getDashboardId().equals(dashboardId)) {
            throw new IllegalStateException("Not allowed to add service owned by dashboard to dependent service!");
        }
        service.getDependedBy().add(dashboardId);
        return serviceRepository.save(service);
    }

    @Override
    public void deleteDependentService(ObjectId dashboardId, ObjectId serviceId) {
        Service service = get(serviceId);
        service.getDependedBy().remove(dashboardId);
        serviceRepository.save(service);
    }
}
