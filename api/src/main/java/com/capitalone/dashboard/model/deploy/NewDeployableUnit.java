package com.capitalone.dashboard.model.deploy;

import java.util.ArrayList;
import java.util.List;

import com.capitalone.dashboard.model.DeploymentMap;
import com.capitalone.dashboard.model.Host;
import com.capitalone.dashboard.model.Pod;

public class NewDeployableUnit {
    private final Pod pod;
    private final String version;
    private final boolean deployed;
    private final String lastUpdated;
    private final String application;
    private final String systemCheck;
    private final boolean ansibleComponentStatus;
    private final String serverCount;
    private final String ansiblelastReported;
    private final String environment;
    private final List<Host> servers = new ArrayList<>();
    
    public NewDeployableUnit(DeploymentMap deploymentMap) {
        this.pod = deploymentMap.getDeployment().getPod();
        this.version = deploymentMap.getVersion();
        this.deployed = deploymentMap.isDeployed();
        this.lastUpdated = deploymentMap.getDeployment().getLastDeploymentTime();
        this.servers.addAll(deploymentMap.getDeployment().getHosts());
        this.application = deploymentMap.getApplication();
        this.systemCheck = deploymentMap.getSystemCheck();
        this.ansibleComponentStatus = deploymentMap.isAnsibleComponentStatusGood();
        this.ansiblelastReported = deploymentMap.getLastUpdated();
        this.environment = deploymentMap.getEnvironment();
        this.serverCount = deploymentMap.getServerCount();
    }

    public String getApplication() {
		return application;
	}

	public String getSystemCheck() {
		return systemCheck;
	}

	public Pod getPod() {
        return pod;
    }

    public String getVersion() {
        return version;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public List<Host> getServers() {
        return servers;
    }

	public boolean isAnsibleComponentStatus() {
		return ansibleComponentStatus;
	}

	public String getServerCount() {
		return serverCount;
	}

	public String getAnsiblelastReported() {
		return ansiblelastReported;
	}

	public String getEnvironment() {
		return environment;
	}
}
