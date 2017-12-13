package com.capitalone.dashboard.datafactory;


import java.io.IOException;
import java.util.List;

import com.capitalone.dashboard.model.AnsibleComponentStatus;
import com.capitalone.dashboard.model.DeploymentMap;
import com.capitalone.dashboard.model.DeploymentTask;
import com.capitalone.dashboard.model.Ivt;
import com.capitalone.dashboard.model.PodVersionMap;
import com.capitalone.dashboard.model.SystemCheckResult;
import com.capitalone.dashboard.model.UDeployApplication;

public interface DeploymentDataFactory {

	 List<DeploymentTask> connectToSplunk() throws InterruptedException, IOException;
	 List<UDeployApplication> createPods(List<DeploymentMap> taskList);
	 List<DeploymentMap> getDeploymentMap(List<DeploymentTask> taskList,List<PodVersionMap> podVersionmapList,List<SystemCheckResult> results);
	 List<PodVersionMap> getVersionData() throws IOException;
	 List<SystemCheckResult> getSystemCheckResults() throws InterruptedException, IOException;
	 List<Ivt> getIvtResults() throws IOException;
	 List<AnsibleComponentStatus> getAnsibleComponentStatusList() throws InterruptedException, IOException;
}
