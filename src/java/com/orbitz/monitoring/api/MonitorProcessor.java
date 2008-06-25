package com.orbitz.monitoring.api;

/**
 * An interface for classes that process monitors that are delivered to the
 * monitoring system.
 *
 * @author Doug Barth
 */
public interface MonitorProcessor {
    /**
     * This is a lifecycle method that a processor can use to initialize itself
     * when the MonitorProcessorFactory starts up. The processor factory passes
     * itself to the MonitorProcessor in case the processor needs to use some
     * service provided by the factory.
     *
     */
    public void startup();

    /**
     * This is a lifecycle method that a processor can use to close any
     * resources it created before the monitoring system shuts down.
     */
    public void shutdown();

    /**
     * This is a method that all monitors will call when they are first created.
     *
     * @param monitor the Monitor that was created
     */
    public void monitorCreated(Monitor monitor);

    /**
     * This is a method that monitors that wrap a unit of work will call when
     * they are started.
     *
     * @param monitor the Monitor that was started
     */
    public void monitorStarted(Monitor monitor);

    /**
     * This is a method that a monitor can use to notify the processor that it
     * is completed and should be processed. All monitors must call this method
     * in order to have themselves processed.
     *
     * @param monitor the Monitor that is ready to be processed
     */
    public void process(Monitor monitor);

    /**
     * Get processor name, used to map levels to processors in the MonitoringEngine.
     *
     * @return Processor name.
     */
    public String getName();
}
