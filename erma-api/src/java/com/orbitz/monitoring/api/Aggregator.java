package com.orbitz.monitoring.api;

public interface Aggregator {

    /**
     * Sends a monitor to the aggregation engine.
     *
     * @param monitor the monitor
     */
    void aggregate(Monitor monitor);

    /**
     * Shut down lifecycle method.
     */
    void shutdown();

    /**
     * Start up lifecycle method.
     */
    void startup();

}
