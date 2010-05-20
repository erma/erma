package com.orbitz.monitoring.lib;

import com.orbitz.monitoring.api.Decomposer;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorFactory;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.engine.StackBasedInheritableStrategy;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.lib.decomposer.AttributeDecomposer;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * A class that allows the MonitoringEngine to be manageable.
 *
 * @author Doug Barth
 *
 * @@org.springframework.jmx.export.metadata.ManagedResource
 * (description="Management interface for ERMA MonitoringEngine")
 */
public class BaseMonitoringEngineManager {
    private static final Logger log = Logger.getLogger(BaseMonitoringEngineManager.class);

    private MonitorProcessorFactory factory;
    private Decomposer decomposer;
    private InheritableStrategy inheritableStrategy;
    private ScheduledExecutorService scheduledExecutor;
    private Map timerTasks;
    private boolean monitoringEnabled = true;

    protected Runnable startupRunnable;

    public BaseMonitoringEngineManager() {
        this(new SimpleMonitorProcessorFactory(null), null);
    }

    public BaseMonitoringEngineManager(MonitorProcessorFactory factory) {
        this(factory, null);
    }

    public BaseMonitoringEngineManager(MonitorProcessorFactory factory,
                                   Decomposer decomposer) {
        if (decomposer == null) {
            decomposer = new AttributeDecomposer();
        }

        this.factory = factory;
        this.decomposer = decomposer;
        this.inheritableStrategy = new StackBasedInheritableStrategy();
    }

    public void startup() {
        MonitoringEngine.getInstance().setMonitoringEnabled(monitoringEnabled);

        MonitoringEngine.getInstance().setProcessorFactory(factory);
        MonitoringEngine.getInstance().setDecomposer(decomposer);
        MonitoringEngine.getInstance().setInheritableStrategy(inheritableStrategy);
        MonitoringEngine.getInstance().setStartupRunnable(startupRunnable);

        if (timerTasks == null) {
            timerTasks = Collections.EMPTY_MAP;
        }

        MonitoringEngine.getInstance().startup();

        EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle", MonitoringLevel.ESSENTIAL);
        monitor.set("eventType", "startup");
        monitor.fire();

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        Iterator iter = timerTasks.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            long millis = ((Number) entry.getKey()).longValue();
            Collection tasks = (Collection) entry.getValue();
            Iterator it = tasks.iterator();
            while (it.hasNext()) {
                Runnable task = (Runnable) it.next();
                scheduledExecutor.scheduleAtFixedRate(task, millis, millis, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void shutdown() {
        scheduledExecutor.shutdown();

        EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle", MonitoringLevel.ESSENTIAL);
        monitor.set("eventType", "shutdown");
        monitor.fire();

        MonitoringEngine.getInstance().shutdown();
    }

    public void reload() {
        EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle", MonitoringLevel.ESSENTIAL);
        monitor.set("eventType", "reload");
        monitor.fire();

        MonitoringEngine.getInstance().restart();
    }

    public Map getTimerTasksMap() {
        return timerTasks;
    }

    /**
     * Takes a map in the form of:
     *   milliseconds -> collection of timer tasks
     *
     * @param timerTasks the map of timer tasks
     */
    public void setTimerTasksMap(Map timerTasks) {
        this.timerTasks = timerTasks;
    }

    public Collection getTimerTasks() {
        return (timerTasks != null && timerTasks.containsKey(new Integer(60000))) ?
                (Collection) timerTasks.get(new Integer(60000)) : Collections.EMPTY_SET;
    }

    /**
     * Takes a collection of timer tasks. 
     *
     * @param timerTasks the map of timer tasks
     */
    public void setTimerTasks(Collection timerTasks) {
        setTimerTasksMap(Collections.singletonMap(new Integer(60000), timerTasks));
    }

    /**
     * Enable/disable all functions of the MonitoringEngine.  When disabled
     * the MonitoringEngine will ignore all Monitor events.
     * 
     * @param monitoringEnabled set to false to disable all Monitor events.
     */
    public void setMonitoringEnabled(boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    /**
     * Get current enabled state of the MonitoringEngine.
     *
     * @return enabled state of MonitoringEngine
     */
    public boolean getMonitoringEnabled() {
        return monitoringEnabled;
    }

    public InheritableStrategy getInheritableStrategy() {
        return inheritableStrategy;
    }

    public void setInheritableStrategy(InheritableStrategy inheritableStrategy) {
        this.inheritableStrategy = inheritableStrategy;
    }

    public void setStartupRunnable(Runnable startupRunnable) {
        this.startupRunnable = startupRunnable;
    }

    /**
     * Get listing of override monitor levels map.
     *
     * @return description of override monitor levels map
     *
     * @@org.springframework.jmx.export.metadata.ManagedAttribute
     * (description="Gets a view into monitor level overrides")
     */
    public String getOverrideMonitorLevelsListing() {
        return MonitoringEngine.getInstance().getOverrideMonitorLevelsListing();
    }

    /**
     * Update MonitoringLevels for a set of monitors
     *
     * @param nameStartsWith either the full name of the monitor or a partial string
     * that will be used to match on the beginning of the monitor name
     * @param levelStr string representation of the monitoring level to set
     *
     * @@org.springframework.jmx.export.metadata.ManagedOperation
     * (description="Sets the monitoring level for the monitor(s)")
     * @@org.springframework.jmx.export.metadata.ManagedOperationParameter
     * (index=0, name="nameStartsWith", description="Apply to all monitor names that start with the given string")
     * @@org.springframework.jmx.export.metadata.ManagedOperationParameter
     * (index=1, name="levelStr", description="Monitoring level to apply to monitor(s)")
     */
     public void updateLevelForMonitor(String nameStartsWith, String levelStr) {
        if (nameStartsWith == null) {
            throw new IllegalArgumentException("nameStartsWith cannot be null");
        } else if (! MonitoringLevel.isValidLevelStr(levelStr)) {
            throw new IllegalArgumentException("levelStr must match an existing MonitoringLevel");
        }

        MonitoringLevel level = MonitoringLevel.toLevel(levelStr);
        MonitoringEngine.getInstance().addMonitorLevel(nameStartsWith, level);

        if (log.isInfoEnabled()) {
            log.info("Added: " + nameStartsWith + " -> " + levelStr +
                " to map of monitor level overrides.");
        }
    }

    /**
     * Assign a MonitoringLevel to a MonitorProcessor.  MonitorProcessor levels override
     * ProcessGroup levels for every group that contains the processor instance.
     *
     * @param name MonitorProcessor name
     * @param levelStr monitoring level
     *
     * @@org.springframework.jmx.export.metadata.ManagedOperation
     * (description="Sets a monitoring level on a MonitorProcessor")
     * @@org.springframework.jmx.export.metadata.ManagedOperationParameter
     * (index=0, name="name", description="processor name as configured in spring bean definition")
     * @@org.springframework.jmx.export.metadata.ManagedOperationParameter
     * (index=1, name="levelStr", description="Monitoring level to apply to processor")     
     */
    public void addLevelForProcessor(String name, String levelStr) {
        if (name == null) {
            throw new IllegalArgumentException("processor name cannot be null");
        } else if (! MonitoringLevel.isValidLevelStr(levelStr)) {
            throw new IllegalArgumentException("levelStr must match an existing MonitoringLevel");
        }

        MonitorProcessor[] processors = factory.getAllProcessors();
        for (int i = 0; i < processors.length; i++) {
            MonitorProcessor processor = processors[i];
            if (name.equalsIgnoreCase(processor.getName())) {
                MonitoringEngine.getInstance().addProcessorLevel(name,
                        MonitoringLevel.toLevel(levelStr));
                log.info("Changed Processor level: " + name + " -> " + levelStr);
                return;//two processors should not have same name
            }
        }
    }

    /**
     * Get listing of MonitorProcessor override levels map.
     *
     * @return description of override processor levels map
     *
     * @@org.springframework.jmx.export.metadata.ManagedAttribute
     * (description="Gets a view into processor level overrides")
     */
    public String getOverrideProcessorLevelsListing() {
        String returnString = MonitoringEngine.getInstance().getOverrideProcessorLevelsListing();
        return returnString;
    }
}
