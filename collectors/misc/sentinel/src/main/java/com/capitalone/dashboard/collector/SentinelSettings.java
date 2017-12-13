package com.capitalone.dashboard.collector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sentinel")
public class SentinelSettings {
private String cron;

public String getCron() {
	return cron;
}

public void setCron(String cron) {
	this.cron = cron;
}

}
