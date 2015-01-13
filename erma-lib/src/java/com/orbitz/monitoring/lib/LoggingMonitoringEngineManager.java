package com.orbitz.monitoring.lib;

import com.orbitz.monitoring.lib.factory.ProcessGroup;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
import com.orbitz.monitoring.lib.processor.LoggingMonitorProcessor;

public class LoggingMonitoringEngineManager extends BaseMonitoringEngineManager {
  public LoggingMonitoringEngineManager() {
    super(new SimpleMonitorProcessorFactory(new ProcessGroup(new LoggingMonitorProcessor())));
  }
}
