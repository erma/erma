package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Samples from a population of monitors
 */
public interface MonitorSampler {

    boolean accept(Monitor monitor);
}
