package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of Collector that stores current build server configuration.
 */
public class SeleniumTestCollector extends Collector {
    private List<String> buildServers = new ArrayList<>();

    public List<String> getBuildServers() {
        return buildServers;
    }

    public void setBuildServers(List<String> buildServers) {
        this.buildServers = buildServers;
    }

    public static SeleniumTestCollector prototype(List<String> buildServers) {
        SeleniumTestCollector protoType = new SeleniumTestCollector();
        protoType.setName("SeleniumCollector");
        protoType.setCollectorType(CollectorType.Test);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getBuildServers().addAll(buildServers);
        return protoType;
    }
}
