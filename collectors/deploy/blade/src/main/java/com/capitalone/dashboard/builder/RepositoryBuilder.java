package com.capitalone.dashboard.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.capitalone.dashboard.repository.AnsibleComponentStatusRepository;
import com.capitalone.dashboard.repository.BladeCollectorRepository;
import com.capitalone.dashboard.repository.DeploymentMapRepository;
import com.capitalone.dashboard.repository.IvtRepository;
import com.capitalone.dashboard.repository.PodRepository;
import com.capitalone.dashboard.repository.PodVersionMapRepository;
import com.capitalone.dashboard.repository.UDeployApplicationRepository;

@Component
public class RepositoryBuilder {
	private final UDeployApplicationRepository uDeployApplicationRepository;
    private final DeploymentMapRepository deploymentMapRepository;
    private final PodVersionMapRepository podVersionMapRepository;
    private final PodRepository podRepository;
    private final IvtRepository ivtRepository;
    private final BladeCollectorRepository bladeCollectorRepository;
    private final AnsibleComponentStatusRepository ansibleComponentStatusRepository;
    
    @Autowired
	public RepositoryBuilder(UDeployApplicationRepository uDeployApplicationRepository,
			DeploymentMapRepository deploymentMapRepository, PodVersionMapRepository podVersionMapRepository,
			PodRepository podRepository, IvtRepository ivtRepository,
			BladeCollectorRepository bladeCollectorRepository,
			AnsibleComponentStatusRepository ansibleComponentStatusRepository) {
		this.uDeployApplicationRepository = uDeployApplicationRepository;
		this.deploymentMapRepository = deploymentMapRepository;
		this.podVersionMapRepository = podVersionMapRepository;
		this.podRepository = podRepository;
		this.ivtRepository = ivtRepository;
		this.bladeCollectorRepository = bladeCollectorRepository;
		this.ansibleComponentStatusRepository = ansibleComponentStatusRepository;
	}
	
	public UDeployApplicationRepository getuDeployApplicationRepository() {
		return uDeployApplicationRepository;
	}
	public DeploymentMapRepository getDeploymentMapRepository() {
		return deploymentMapRepository;
	}
	public PodVersionMapRepository getPodVersionMapRepository() {
		return podVersionMapRepository;
	}
	public PodRepository getPodRepository() {
		return podRepository;
	}
	public IvtRepository getIvtRepository() {
		return ivtRepository;
	}
	public BladeCollectorRepository getBladeCollectorRepository() {
		return bladeCollectorRepository;
	}
	public AnsibleComponentStatusRepository getAnsibleComponentStatusRepository() {
		return ansibleComponentStatusRepository;
	}
	
}
