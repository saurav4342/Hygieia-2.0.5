package com.capitalone.dashboard.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class FlowController {
	 @RequestMapping(value = "/flow", method = GET, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> authenticate(){
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange("https://api.flowdock.com/oauth/authorize", HttpMethod.GET,new HttpEntity<>(new HttpHeaders()),String.class);
	}
}
