package com.orbitz.monitoring.api;

import java.util.Set;

/**
 * The interface that can be implemented to hook into the process of choosing
 * the processors that are applicable for a given Monitor object.
 *
 * @author Doug Barth
 */
public interface MonitorProcessorFactory {
  /**
   * This is a lifecycle method that will be called when the MonitoringEngine
   * is started.
   */
  public void startup();

  /**
   * This is a lifecycle method that will be called when the MonitoringEngine
   * is shutdown.
   */
  public void shutdown();

  /**
   * This is the method that will return the MonitorProcessor instances that
   * should be called to process this monitor.
   *
   * @param monitor the monitor to process
   * @return The processors that are applicable for this monitor
   */
  public MonitorProcessor[] getProcessorsForMonitor(Monitor monitor);

  /**
   * Retrieve an array of all the MonitorProcessors for this MPF
   * @return The processors for this MPF
   */
  public MonitorProcessor[] getAllProcessors();

  /**
   * Retrieve a set of all the MonitorProcessors for this MPF whose names' match the name 
   * parameter.
   * @param name
   * @return The processors for this MPF
   */
  public Set<MonitorProcessor> getProcessorsByName(String name);
}
