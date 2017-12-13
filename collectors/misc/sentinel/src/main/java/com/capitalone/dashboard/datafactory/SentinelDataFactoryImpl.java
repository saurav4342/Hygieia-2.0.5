package com.capitalone.dashboard.datafactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.capitalone.dashboard.model.PodStatus;
@Component
public class SentinelDataFactoryImpl implements SentinelDataFactory{

	public List<PodStatus> getPodStatus() throws IOException{
		List<PodStatus> podStatusList = new ArrayList<PodStatus>();
		String command = "powershell.exe -file C:\\Users\\nayaksau\\Desktop\\demo.ps1";
		// Executing the command
		Process powerShellProcess = Runtime.getRuntime().exec(command);
		// Getting the results
		powerShellProcess.getOutputStream().close();
		String line;
		BufferedReader stdout = new BufferedReader(new InputStreamReader(
				powerShellProcess.getInputStream()));
		while ((line = stdout.readLine()) != null) {
			podStatusList.add(createPodStatusData(line));
		}
		stdout.close();
		/* BufferedReader stderr = new BufferedReader(new InputStreamReader(
  	    powerShellProcess.getErrorStream()));
  	  while ((line = stderr.readLine()) != null) {
  	   continue;
  	  }*/
		//  	  stderr.close();
		return podStatusList;
	} 
	
	public PodStatus createPodStatusData(String line){
		String [] arr = line.split("-");
		PodStatus podStatus = new PodStatus();
		podStatus.setPodName(arr[0]);
		podStatus.setServerName(arr[1]);
		podStatus.setServiceName(arr[2]);
		podStatus.setStatus(arr[3]);
		return podStatus;
	}
}
