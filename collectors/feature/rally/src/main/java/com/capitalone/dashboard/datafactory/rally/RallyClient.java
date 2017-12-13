package com.capitalone.dashboard.datafactory.rally;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

import com.rallydev.rest.RallyRestApi;
@Component
public class RallyClient {
	
	public RallyRestApi getRallyClient(String rallyUrl,String apiKey) throws URISyntaxException
	{
		URI url = new URI(rallyUrl);
		String key = apiKey;
		return new RallyRestApi(url,key);
		}
}
