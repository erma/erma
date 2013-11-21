package com.orbitz.monitoring.lib.factory;

import java.util.Collections;
import java.util.List;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringLevel;

/**
 * An object that contains the configuration for what processors should be called for which
 * monitors.
 * 
 * @@org.springframework.jmx.export.metadata.ManagedResource 
 *                                                           (description="ProcessGroup can be enabled/disabled and MonitoringLevel adjusted"
 *                                                           )
 */
@ManagedResource(description = "ProcessGroup can be enabled/disabled and MonitoringLevel adjusted")
public class ProcessGroup {
  private static final Logger log = Logger.getLogger(ProcessGroup.class);
  
  private MonitoringLevel _monitoringLevel = MonitoringLevel.INFO;
  private boolean _active = true;
  private Expression _appliesExpression;
  private final List<MonitorProcessor> _processors;
  
  public ProcessGroup(final MonitorProcessor processor) {
    this(Lists.newArrayList(processor));
  }
  
  public ProcessGroup(final MonitorProcessor[] processors) {
    _processors = Lists.newArrayList(processors);
  }
  
  public ProcessGroup(final List<MonitorProcessor> processors) {
    _processors = processors;
  }
  
  /**
   * Returns the list of processors within this ProcessGroup that apply for the MonitoringLevel of
   * the given monitor.
   * 
   * @param monitor Monitor instance ready for processing
   * @return list of MonitorProcessors
   */
  public Iterable<MonitorProcessor> getProcessorsFor(final Monitor monitor) {
    if (!isActive()) {
      return Collections.emptyList();
    }
    final MonitoringLevel monitorLevel = monitor.getLevel();
    // Use a filter here because it creates a view of the existing list instead of making a copy
    Iterable<MonitorProcessor> processorsForMonitor = Iterables.filter(_processors,
        new Predicate<MonitorProcessor>() {
          public boolean apply(final MonitorProcessor processor) {
            MonitoringLevel processorLevel = processor.getLevel();
            if (processorLevel != null) {
              return monitorLevel.hasHigherOrEqualPriorityThan(processorLevel);
            }
            else {
              return monitorLevel.hasHigherOrEqualPriorityThan(_monitoringLevel);
            }
          }
        });
    if (processorsForMonitor.iterator().hasNext()) {
      if (!matchesExpressionFor(monitor)) {
        return Collections.emptyList();
      }
    }
    return processorsForMonitor;
  }
  
  /**
   * appliesTo will determine if this ProcessGroup should have the monitor.
   * @param monitor being processed
   * @return true if the monitor will be handled by this process group, else false
   */
  @VisibleForTesting
  boolean matchesExpressionFor(final Monitor monitor) {
    boolean applies = true;
    if (_appliesExpression != null) {
      JexlContext context = JexlHelper.createContext();
      context.getVars().put("m", monitor);
      context.getVars().putAll(monitor.getAll());
      try {
        Object result = _appliesExpression.evaluate(context);
        if (result != null && result instanceof Boolean) {
          Boolean expressionResult = (Boolean)result;
          applies = expressionResult.booleanValue();
        }
        else {
          applies = false;
        }
      }
      catch (Exception e) {
        log.debug("Exception while applying expression: ", e);
        applies = false;
      }
    }
    
    return applies;
  }
  
  public void setExpression(final String expressionString) {
    Expression expression = null;
    
    if (expressionString != null) {
      try {
        expression = ExpressionFactory.createExpression(expressionString);
      }
      catch (Exception e) {
        log.error("Error setting expression: ", e);
      }
    }
    
    _appliesExpression = expression;
  }
  
  /**
   * @@org.springframework.jmx.export.metadata.ManagedAttribute 
   *                                                            (description="Returns true if this process group is enabled"
   *                                                            )
   */
  @ManagedAttribute(description = "Returns true if this process group is enabled")
  public boolean isActive() {
    return _active;
  }
  
  /**
   * @@org.springframework.jmx.export.metadata.ManagedAttribute 
   *                                                            (description="Set to true/false to activate/deactivate the process group"
   *                                                            )
   * @@org.springframework.jmx.export.metadata.ManagedOperationParameter (index=0, name="active",
   *                                                                     description=
   *                                                                     "boolean value for activating the process group"
   *                                                                     )
   */
  @ManagedAttribute(description = "Set to true/false to activate/deactivate the process group")
  public void setActive(final boolean active) {
    _active = active;
    
    log.info(this.toString() + (active ? " activated" : " deactivated"));
  }
  
  /**
   * Gets all processors
   * @return the processors
   * @deprecated use {@link #getAllProcessors()} instead
   */
  @Deprecated
  public MonitorProcessor[] getProcessors() {
    return _processors.toArray(new MonitorProcessor[0]);
  }
  
  /**
   * Gets all processors
   * @return the processors
   */
  public List<MonitorProcessor> getAllProcessors() {
    return _processors;
  }
  
  /**
   * @@org.springframework.jmx.export.metadata.ManagedAttribute 
   *                                                            (description="get the string representation of the monitoring level for this process group"
   *                                                            )
   */
  @ManagedAttribute(description = "get the string representation of the monitoring level for this process group")
  public String getMonitoringLevel() {
    return _monitoringLevel.toString();
  }
  
  /**
   * @@org.springframework.jmx.export.metadata.ManagedOperation 
   *                                                            (description="Set the monitoring level for this process group"
   *                                                            )
   * @@org.springframework.jmx.export.metadata.ManagedOperationParameter (index=0,
   *                                                                     name="levelString",
   *                                                                     description=
   *                                                                     "new MonitoringLevel to apply"
   *                                                                     )
   */
  @ManagedOperation(description = "Set the monitoring level for this process group")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "levelString", description = "new MonitoringLevel to apply")})
  public void updateMonitoringLevel(final String levelString) {
    if (!MonitoringLevel.isValidLevelStr(levelString)) {
      throw new IllegalArgumentException("levelString must match an existing MonitoringLevel");
    }
    
    _monitoringLevel = MonitoringLevel.toLevel(levelString);
    
    log.info(this.toString() + " -> " + levelString);
  }
  
  /**
   * Used for spring wiring, just wraps the above runtime method
   * @param levelString new MonitoringLevel to apply
   */
  public void setMonitoringLevel(final String levelString) {
    updateMonitoringLevel(levelString);
  }
}
