package com.capitalone.dashboard.model;
/**
 * Collector implementation for UDeploy that stores UDeploy server URLs.
 */
public class SentinelCollector extends Collector {
    public static SentinelCollector prototype() {
        SentinelCollector protoType = new SentinelCollector();
        protoType.setName("Sentinel");
        protoType.setCollectorType(CollectorType.Deployment);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        return protoType;
    }
}
