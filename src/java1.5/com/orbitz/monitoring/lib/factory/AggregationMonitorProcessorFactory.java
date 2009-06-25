package com.orbitz.monitoring.lib.factory;

import com.orbitz.monitoring.api.Aggregator;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.processor.AggregationMonitorProcessor;

import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * An implementation of MonitorProcessorFactory that supports a separate
 * aggregation process group.
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
    public AggregationMonitorProcessorFactory(ProcessGroup[] processGroups, Aggregator aggregator, Set<Class<? extends Monitor>> clazzes) {
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
     *
     * @param monitor the monitor
     * @return the array of monitor processors
     */
    public MonitorProcessor[] getProcessorsForMonitor(Monitor monitor) {
        if(clazzes.contains(monitor.getClass())) {
            final List<MonitorProcessor> processors = aggregationGroup.getProcessorsFor(monitor);
            return processors.toArray(new MonitorProcessor[processors.size()]);
        } else {
            return super.getProcessorsForMonitor(monitor);
        }
    }

    /**
     * Start up lifecycle method.
     */
    public void startup() {
        super.startup();
        for(MonitorProcessor processor : aggregationGroup.getProcessors()) {
            processor.startup();
        }
    }

    /**
     * Shut down lifecycle method.
     */
    public void shutdown() {
        for(MonitorProcessor processor : aggregationGroup.getProcessors()) {
            processor.shutdown();
        }
        super.shutdown();
    }

    /**
     * Set the expression for the aggregation process group.
     *
     * @param expression the expression
     */
    public void setAggregationExpression(String expression) {
        aggregationGroup.setExpression(expression);
    }

    /**
     * Set the level for the aggregation process group.
     *
     * @param levelString the level
     */
    public void setAggregationMonitoringLevel(String levelString) {
        aggregationGroup.setMonitoringLevel(levelString);
    }

}
