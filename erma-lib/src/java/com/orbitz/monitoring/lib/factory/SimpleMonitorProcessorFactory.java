package com.orbitz.monitoring.lib.factory;

import com.google.common.collect.Lists;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorFactory;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An implementation of MonitorProcessorFactory that needs to be conifgured programmatically.
 * 
 * @author Doug Barth
 */
public class SimpleMonitorProcessorFactory implements MonitorProcessorFactory {
  // ** PRIVATE DATA ********************************************************
  private final ProcessGroup[] _processGroups;
  
  // ** CONSTRUCTORS ********************************************************
  public SimpleMonitorProcessorFactory(ProcessGroup... processGroups) {
    if (processGroups == null) {
      processGroups = new ProcessGroup[0];
    }
    _processGroups = processGroups;
  }
  
  // ** PUBLIC METHODS ******************************************************
  public void startup() {
    Set allMps = getAllMonitorProcessors();
    for (Iterator i = allMps.iterator(); i.hasNext();) {
      MonitorProcessor processor = (MonitorProcessor)i.next();
      processor.startup();
    }
  }
  
  public void shutdown() {
    Set allMps = getAllMonitorProcessors();
    for (Iterator i = allMps.iterator(); i.hasNext();) {
      MonitorProcessor processor = (MonitorProcessor)i.next();
      processor.shutdown();
    }
  }
  
  public MonitorProcessor[] getProcessorsForMonitor(final Monitor monitor) {
    Set applicableProcessors = new LinkedHashSet();
    for (int i = 0; i < _processGroups.length; i++) {
      ProcessGroup processGroup = _processGroups[i];
      for (MonitorProcessor processor : processGroup.getProcessorsFor(monitor)) {
        applicableProcessors.add(processor);
      }
    }
    return (MonitorProcessor[])applicableProcessors
        .toArray(new MonitorProcessor[applicableProcessors.size()]);
  }
  
  /**
   * Finds processors that will handle the specified monitor
   * @param monitor the monitor to be processed
   * @return the monitor processors
   */
  public Iterable<MonitorProcessor> findProcessorsForMonitor(final Monitor monitor) {
    return Lists.newArrayList(this.getProcessorsForMonitor(monitor));
  }
  
  public MonitorProcessor[] getAllProcessors() {
    Set allMps = getAllMonitorProcessors();
    
    return (MonitorProcessor[])allMps.toArray(new MonitorProcessor[allMps.size()]);
  }
  
  public Set getAllMonitorProcessors() {
    Set allMps = new LinkedHashSet();
    for (int i = 0; i < _processGroups.length; i++) {
      ProcessGroup processGroup = _processGroups[i];
      allMps.addAll(Arrays.asList(processGroup.getProcessors()));
    }
    
    return allMps;
  }
}
