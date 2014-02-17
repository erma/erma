package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringLevel;

/**
 * An abstract adapter class for MonitorProcessors. The methods in this class are empty. This class exists as
 * convenience for creating MonitorProcessor objects; subclass it and override what you
 * need.
 *
 * @since 3.5
 *
 * @author Matt O'Keefe
 */
public abstract class MonitorProcessorAdapter implements MonitorProcessor {
    private MonitoringLevel level;

    /**
     * This is a lifecycle method that a processor can use to initialize itself
     * when the MonitorProcessorFactory starts up. The processor factory passes
     * itself to the MonitorProcessor in case the processor needs to use some
     * service provided by the factory.
     */
    public void startup() {

    }

    /**
     * This is a lifecycle method that a processor can use to close any
     * resources it created before the monitoring system shuts down.
     */
    public void shutdown() {

    }

    /**
     * This is a method that all monitors will call when they are first created.
     *
     * @param monitor
     */
    public void monitorCreated(Monitor monitor) {

    }

    /**
     * This is a method that monitors that wrap a unit of work will call when
     * they are started.
     *
     * @param monitor
     */
    public void monitorStarted(Monitor monitor) {

    }

    /**
     * This is a method that a monitor can use to notify the processor that it
     * is completed and should be processed. All monitors must call this method
     * in order to have themselves processed.
     *
     * @param monitor
     */
    public void process(Monitor monitor) {
        
    }

    public String getName() {
        return "";
    }

    @Override
    public void setLevel(MonitoringLevel level) {
        this.level = level;
    }

    @Override
    public MonitoringLevel getLevel() {
        return level;
    }
}
