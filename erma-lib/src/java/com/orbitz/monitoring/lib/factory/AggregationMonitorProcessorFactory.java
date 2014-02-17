package com.orbitz.monitoring.lib.factory;

import com.orbitz.monitoring.api.Aggregator;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.lib.processor.AggregationMonitorProcessor;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An implementation of MonitorProcessorFactory that supports a separate aggregation process group.
 */
public class AggregationMonitorProcessorFactory extends SimpleMonitorProcessorFactory {
  
  private final Set<Class<? extends Monitor>> clazzes;
  private final ProcessGroup aggregationGroup;
  
  /**
   * Create a new aggregation monitor processor.
   * 
   * @param processGroups the non-aggregation process groups
   * @param aggregator the aggregator
   * @param clazzes the set of aggregated classes
   */
  public AggregationMonitorProcessorFactory(final ProcessGroup[] processGroups,
      final Aggregator aggregator, final Set<Class<? extends Monitor>> clazzes) {
    super(processGroups);
    this.clazzes = Collections.unmodifiableSet(clazzes);
    this.aggregationGroup = new ProcessGroup(new AggregationMonitorProcessor(aggregator));
  }
  
  /**
   * Gets the set of classes to be aggregated.
   * 
   * @return the set of classes
   */
  public Set<Class<? extends Monitor>> getClasses() {
    return clazzes;
  }
  
  /**
   * The monitor processors that apply to the monitor.
   * @param monitor the monitor
   * @return the array of monitor processors
   * @deprecated use {@link #findProcessorsForMonitor(Monitor)}
   */
  @Override
  @Deprecated
  public MonitorProcessor[] getProcessorsForMonitor(final Monitor monitor) {
    if (clazzes.contains(monitor.getClass())) {
      final List<MonitorProcessor> processors = Lists.newArrayList(aggregationGroup
          .getProcessorsFor(monitor));
      return processors.toArray(new MonitorProcessor[processors.size()]);
    }
    else {
      return super.getProcessorsForMonitor(monitor);
    }
  }
  
  /**
   * The monitor processors that apply to the monitor.
   * @param monitor the monitor
   * @return the array of monitor processors
   */
  @Override
  public Iterable<MonitorProcessor> findProcessorsForMonitor(final Monitor monitor) {
    if (clazzes.contains(monitor.getClass())) {
      return aggregationGroup.getProcessorsFor(monitor);
    }
    else {
      return super.findProcessorsForMonitor(monitor);
    }
  }
  
  /**
   * Start up lifecycle method.
   */
  @Override
  public void startup() {
    super.startup();
    for (MonitorProcessor processor : aggregationGroup.getAllProcessors()) {
      processor.startup();
    }
  }
  
  /**
   * Shut down lifecycle method.
   */
  @Override
  public void shutdown() {
    for (MonitorProcessor processor : aggregationGroup.getAllProcessors()) {
      processor.shutdown();
    }
    super.shutdown();
  }
  
  /**
   * Set the expression for the aggregation process group.
   * 
   * @param expression the expression
   */
  public void setAggregationExpression(final String expression) {
    aggregationGroup.setExpression(expression);
  }
  
  /**
   * Set the level for the aggregation process group.
   * 
   * @param levelString the level
   */
  public void setAggregationMonitoringLevel(final String levelString) {
    aggregationGroup.setMonitoringLevel(levelString);
  }
  
}
