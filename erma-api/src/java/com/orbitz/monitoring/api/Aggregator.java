package com.orbitz.monitoring.api;

/**
 * Collects monitors<br />
 * Created May 20, 2010
 * @author Greg Opaczewski
 */
public interface Aggregator {
    
    /**
     * Sends a monitor to the aggregation engine.
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
