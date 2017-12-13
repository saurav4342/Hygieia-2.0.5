package com.capitalone.dashboard.datafactory;

import java.io.IOException;
import java.util.List;

import com.capitalone.dashboard.model.TestResult;

public interface SeleniumTestDataFactory {
List<TestResult> getTestResult() throws IOException;
}
