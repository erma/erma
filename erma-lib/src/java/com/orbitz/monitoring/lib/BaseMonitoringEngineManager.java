package com.orbitz.monitoring.lib;

import com.orbitz.monitoring.api.Decomposer;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorFactory;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.engine.StackBasedInheritableStrategy;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.lib.decomposer.AttributeDecomposer;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Allows {@link MonitoringEngine} to be manageable.
 * @author Doug Barth
 * @@org.springframework.jmx.export.metadata.ManagedResource 
 *                                                           (description="Management interface for ERMA MonitoringEngine"
 *                                                           )
 */
@ManagedResource(description = "Management interface for ERMA MonitoringEngine")
// TODO: Remove the Javadoc annotation
public class BaseMonitoringEngineManager {
  private final Decomposer decomposer;
  
  private final MonitorProcessorFactory factory;
  private InheritableStrategy inheritableStrategy;
  private final Logger log = Logger.getLogger(BaseMonitoringEngineManager.class);
  private boolean monitoringEnabled = true;
  private ScheduledExecutorService scheduledExecutor;
  /**
   * Set into the {@link MonitoringEngine} using
   * {@link MonitoringEngine#setStartupRunnable(Runnable)}
   */
  protected Runnable startupRunnable;
  private Map<Integer, Collection<TimerTask>> timerTasks;
  
  /**
   * Creates a {@link BaseMonitoringEngineManager} with an empty
   * {@link SimpleMonitorProcessorFactory}
   */
  public BaseMonitoringEngineManager() {
    this(new SimpleMonitorProcessorFactory(null), null);
  }
  
  /**
   * Creates a {@link BaseMonitoringEngineManager}
   * @param factory used to create {@link MonitorProcessor MonitorProcessors}
   */
  public BaseMonitoringEngineManager(final MonitorProcessorFactory factory) {
    this(factory, null);
  }
  
  /**
   * Creates a {@link BaseMonitoringEngineManager}
   * @param factory used to create {@link MonitorProcessor MonitorProcessors}
   * @param decomposer used to make properties of monitors {@link Serializable}
   */
  public BaseMonitoringEngineManager(final MonitorProcessorFactory factory, Decomposer decomposer) {
    if (decomposer == null) {
      decomposer = new AttributeDecomposer();
    }
    
    this.factory = factory;
    this.decomposer = decomposer;
    this.inheritableStrategy = new StackBasedInheritableStrategy();
  }
  
  /**
   * Assign a MonitoringLevel to a MonitorProcessor. MonitorProcessor levels override ProcessGroup
   * levels for every group that contains the processor instance.
   * 
   * @param name MonitorProcessor name
   * @param levelStr monitoring level
   * 
   * @@org.springframework.jmx.export.metadata.ManagedOperation 
   *                                                            (description="Sets a monitoring level on a MonitorProcessor"
   *                                                            )
   * @@org.springframework.jmx.export.metadata.ManagedOperationParameter (index=0, name="name",
   *                                                                     description=
   *                                                                     "processor name as configured in spring bean definition"
   *                                                                     )
   * @@org.springframework.jmx.export.metadata.ManagedOperationParameter (index=1, name="levelStr",
   *                                                                     description=
   *                                                                     "Monitoring level to apply to processor"
   *                                                                     )
   */
  @ManagedOperation(description = "Sets a monitoring level on a MonitorProcessor")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "name", description = "processor name as configured in spring bean definition"),
      @ManagedOperationParameter(name = "levelStr", description = "Monitoring level to apply to processor")})
  // TODO: Remove the Javadoc annotation
  public void addLevelForProcessor(final String name, final String levelStr) {
    if (name == null) {
      throw new IllegalArgumentException("processor name cannot be null");
    }
    else if (!MonitoringLevel.isValidLevelStr(levelStr)) {
      throw new IllegalArgumentException("levelStr must match an existing MonitoringLevel");
    }
    
    MonitorProcessor[] processors = factory.getAllProcessors();
    for (int i = 0; i < processors.length; i++) {
      MonitorProcessor processor = processors[i];
      if (name.equalsIgnoreCase(processor.getName())) {
        MonitoringEngine.getInstance().addProcessorLevel(name, MonitoringLevel.toLevel(levelStr));
        log.info("Changed Processor level: " + name + " -> " + levelStr);
        return;// two processors should not have same name
      }
    }
  }
  
  /**
   * Determines what a {@link Monitor} inherits from its parent(s)
   * @return the strategy
   */
  public InheritableStrategy getInheritableStrategy() {
    return inheritableStrategy;
  }
  
  /**
   * Gets the value that is used to determine whether monitoring will be enabled for the
   * {@link MonitoringEngine} when {@link #startup()} is called.
   * @return true if monitoring should be enabled for the {@link MonitoringEngine}, false otherwise
   */
  public boolean getMonitoringEnabled() {
    return monitoringEnabled;
  }
  
  /**
   * Gets the override monitor levels by calling
   * {@link MonitoringEngine#getOverrideMonitorLevelsListing()}
   * @return description of override monitor levels map
   * @see MonitoringEngine#getOverrideMonitorLevelsListing()
   * @@org.springframework.jmx.export.metadata.ManagedAttribute 
   *                                                            (description="Gets a view into monitor level overrides"
   *                                                            )
   */
  @ManagedAttribute(description = "Gets a view into monitor level overrides")
  // TODO: Remove the Javadoc annotation
  public String getOverrideMonitorLevelsListing() {
    return MonitoringEngine.getInstance().getOverrideMonitorLevelsListing();
  }
  
  /**
   * Gets the override processor levels by calling
   * {@link MonitoringEngine#getOverrideProcessorLevelsListing()}
   * @return description of override processor levels map
   * @see MonitoringEngine#getOverrideProcessorLevelsListing()
   * @@org.springframework.jmx.export.metadata.ManagedAttribute 
   *                                                            (description="Gets a view into processor level overrides"
   *                                                            )
   */
  @ManagedAttribute(description = "Gets a view into processor level overrides")
  public String getOverrideProcessorLevelsListing() {
    String returnString = MonitoringEngine.getInstance().getOverrideProcessorLevelsListing();
    return returnString;
  }
  
  /**
   * Gets the timer tasks that will be scheduled to run every minute when {@link #startup()} is
   * called.<br>
   * <i>Note that any timer tasks scheduled to run at any frequency other than one minute will not
   * be returned.</i>
   * @return the minutely timer tasks
   */
  public Collection<TimerTask> getTimerTasks() {
    if (timerTasks != null && timerTasks.containsKey(new Integer(60000))) {
      return timerTasks.get(new Integer(60000));
    }
    return Collections.emptyList();
  }
  
  /**
   * Gets all timer tasks<br>
   * <ul>
   * <li>
   * <b>60000</b>
   * <ul>
   * <li>Task run every minute</li>
   * <li>Task run every minute</li>
   * <li>Task run every minute</li>
   * </ul>
   * </li>
   * <li>
   * <b>1000</b>
   * <ul>
   * <li>Task run every second</li>
   * </ul>
   * </li>
   * <li>
   * <b>3600000</b>
   * <ul>
   * <li>Task run every hour</li>
   * </ul>
   * </li>
   * </ul>
   * @return a map of timer tasks in which the keys are the frequencies, in milliseconds, at which
   *         each task is run and values are collections of tasks to run at those frequencies.
   */
  public Map<Integer, Collection<TimerTask>> getTimerTasksMap() {
    return timerTasks;
  }
  
  /**
   * Restarts the {@link MonitoringEngine}
   * @see MonitoringEngine#restart()
   */
  public void reload() {
    EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle",
        MonitoringLevel.ESSENTIAL);
    monitor.set("eventType", "reload");
    monitor.fire();
    MonitoringEngine.getInstance().restart();
  }
  
  /**
   * Sets the strategy that determines whether {@link Monitor} attributes are inherited by their
   * children
   * @param inheritableStrategy the strategy
   */
  public void setInheritableStrategy(final InheritableStrategy inheritableStrategy) {
    this.inheritableStrategy = inheritableStrategy;
  }
  
  /**
   * Sets the value that determines whether the {@link MonitoringEngine} will be enabled when
   * {@link #startup()} is called.
   * @param monitoringEnabled true to enable monitoring, false to cause the {@link MonitoringEngine}
   *        to ignore all monitors
   * @see MonitoringEngine#setMonitoringEnabled(boolean)
   */
  public void setMonitoringEnabled(final boolean monitoringEnabled) {
    this.monitoringEnabled = monitoringEnabled;
  }
  
  /**
   * Sets the runnable that will be set using {@link MonitoringEngine#setStartupRunnable(Runnable)}
   * when {@link #startup()} is called.
   * @param startupRunnable the startup runnable
   */
  public void setStartupRunnable(final Runnable startupRunnable) {
    this.startupRunnable = startupRunnable;
  }
  
  /**
   * Sets timer tasks that will be executed every minute<br>
   * <i>Note that this will overwrite existing timer tasks.</i>
   * @param timerTasks timer tasks to set
   */
  public void setTimerTasks(final Collection<TimerTask> timerTasks) {
    setTimerTasksMap(Collections.singletonMap(new Integer(60000), timerTasks));
  }
  
  /**
   * Sets the timer tasks
   * @param timerTasks the map of timer tasks, in which keys are the frequencies at which tasks are
   *        executed and keys are collections of the tasks to execute at those frequencies
   * @see #getTimerTasksMap()
   */
  public void setTimerTasksMap(final Map<Integer, Collection<TimerTask>> timerTasks) {
    this.timerTasks = timerTasks;
  }
  
  /**
   * Stops all {@link #getTimerTasks() timer tasks} and {@link MonitoringEngine#shutdown() shuts the
   * monitoring engine down}.
   */
  public void shutdown() {
    scheduledExecutor.shutdown();
    EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle",
        MonitoringLevel.ESSENTIAL);
    monitor.set("eventType", "shutdown");
    monitor.fire();
    MonitoringEngine.getInstance().shutdown();
  }
  
  /**
   * Sets all properties of the {@link MonitoringEngine} that have been configured, such as
   * {@link #getMonitoringEnabled()} and {@link #getInheritableStrategy()}, then starts it. After
   * the {@link MonitoringEngine} is started, schedules all {@link #getTimerTasks() timer tasks}.
   */
  public void startup() {
    MonitoringEngine.getInstance().setMonitoringEnabled(monitoringEnabled);
    MonitoringEngine.getInstance().setProcessorFactory(factory);
    MonitoringEngine.getInstance().setDecomposer(decomposer);
    MonitoringEngine.getInstance().setInheritableStrategy(inheritableStrategy);
    MonitoringEngine.getInstance().setStartupRunnable(startupRunnable);
    if (timerTasks == null) {
      timerTasks = Collections.emptyMap();
    }
    MonitoringEngine.getInstance().startup();
    EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle",
        MonitoringLevel.ESSENTIAL);
    monitor.set("eventType", "startup");
    monitor.fire();
    scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    for (Entry<Integer, Collection<TimerTask>> entry : timerTasks.entrySet()) {
      long millis = entry.getKey();
      for (Runnable task : entry.getValue()) {
        scheduledExecutor.scheduleAtFixedRate(task, millis, millis, TimeUnit.MILLISECONDS);
      }
    }
  }
  
  /**
   * Sets a monitoring level in the {@link MonitoringEngine}
   * @param nameStartsWith either the full name of the monitor or a partial string that will be used
   *        to match on the beginning of the monitor name
   * @param levelStr string representation of the monitoring level to set
   * @see MonitoringEngine#addMonitorLevel(String, MonitoringLevel)
   * @@org.springframework.jmx.export.metadata.ManagedOperation 
   *                                                            (description="Sets the monitoring level for the monitor(s)"
   *                                                            )
   * @@org.springframework.jmx.export.metadata.ManagedOperationParameter (index=0,
   *                                                                     name="nameStartsWith",
   *                                                                     description=
   *                                                                     "Apply to all monitor names that start with the given string"
   *                                                                     )
   * @@org.springframework.jmx.export.metadata.ManagedOperationParameter (index=1, name="levelStr",
   *                                                                     description=
   *                                                                     "Monitoring level to apply to monitor(s)"
   *                                                                     )
   */
  @ManagedOperation(description = "Sets the monitoring level for the monitor(s)")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "nameStartsWith", description = "Apply to all monitor names that start with the given string"),
      @ManagedOperationParameter(name = "levelStr", description = "Monitoring level to apply to monitor(s)")})
  // TODO: Remove the Javadoc annotation
  public void updateLevelForMonitor(final String nameStartsWith, final String levelStr) {
    if (nameStartsWith == null) {
      throw new IllegalArgumentException("nameStartsWith cannot be null");
    }
    else if (!MonitoringLevel.isValidLevelStr(levelStr)) {
      throw new IllegalArgumentException("levelStr must match an existing MonitoringLevel");
    }
    MonitoringLevel level = MonitoringLevel.toLevel(levelStr);
    MonitoringEngine.getInstance().addMonitorLevel(nameStartsWith, level);
    if (log.isInfoEnabled()) {
      log.info("Added: " + nameStartsWith + " -> " + levelStr
          + " to map of monitor level overrides.");
    }
  }
}
