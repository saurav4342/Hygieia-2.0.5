package com.capitalone.dashboard.datafactory.rally;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.Milestone;

public interface RallyDataFactory {

	List<Milestone> getMilestones() throws URISyntaxException, IOException;
	List<Feature> getFeaturesForMilestones(List<Milestone> milestones) throws URISyntaxException, IOException;
	List<Feature> getDeferredDefectsForMilestones(List<Milestone> milestones) throws URISyntaxException, IOException;
}
