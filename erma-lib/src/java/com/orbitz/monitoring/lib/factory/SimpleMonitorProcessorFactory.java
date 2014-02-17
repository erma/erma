package com.orbitz.monitoring.lib.factory;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorFactory;

import com.google.common.collect.Lists;

import java.util.HashSet;
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
    for (MonitorProcessor processor : getAllMonitorProcessors()) {
      processor.startup();
    }
  }
  
  public void shutdown() {
      for (MonitorProcessor processor : getAllMonitorProcessors()) {
          processor.shutdown();
        }
      }
  
  public MonitorProcessor[] getProcessorsForMonitor(final Monitor monitor) {
      Set<MonitorProcessor> applicableProcessors = new LinkedHashSet<MonitorProcessor>();
      for (ProcessGroup processGroup : _processGroups) {
          for (MonitorProcessor processor : processGroup.getProcessorsFor(monitor)) {
            applicableProcessors.add(processor);
          }
      }
   
      return applicableProcessors.toArray(new MonitorProcessor[applicableProcessors.size()]);
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
    Set<MonitorProcessor> allMps = getAllMonitorProcessors();
    
    return allMps.toArray(new MonitorProcessor[allMps.size()]);
  }
  
  public Set<MonitorProcessor> getAllMonitorProcessors() {
    Set<MonitorProcessor> allMps = new LinkedHashSet<MonitorProcessor>();
    for (int i = 0; i < _processGroups.length; i++) {
      ProcessGroup processGroup = _processGroups[i];
      allMps.addAll(processGroup.getAllProcessors());
    }
    
    return allMps;
  }

    @Override
    public Set<MonitorProcessor> getProcessorsByName(String name) {
        Set<MonitorProcessor> processors = new HashSet<MonitorProcessor>();
        for (MonitorProcessor processor : (Set<MonitorProcessor>)getAllMonitorProcessors()) {
            if (processor.getName().equals(name)) {
                processors.add(processor);
            }
        }
        
        return processors;
    }
}
