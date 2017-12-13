package com.capitalone.dashboard.datafactory;

import java.io.IOException;
import java.util.List;

import com.capitalone.dashboard.model.PodStatus;

public interface SentinelDataFactory {
List<PodStatus> getPodStatus() throws IOException;
}
