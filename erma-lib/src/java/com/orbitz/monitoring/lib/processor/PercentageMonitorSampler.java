package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.text.NumberFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MonitorSampler that accepts a defined percentage of all monitors
 *
 * @author Greg Opaczewski
 */
@ManagedResource(description="PercentageMonitorSampler mbean")
public class PercentageMonitorSampler implements MonitorSampler {

    private static final Logger logger = Logger.getLogger(PercentageMonitorSampler.class.getName());

    private float sampleRate;

    private Lock sampleRateLock = new ReentrantLock();
    private int populationCount;
    private int sampledCount;

    public PercentageMonitorSampler(float samplePercentage) {
        setSamplePercentageInternal(samplePercentage);
    }

    /**
     * Accepts the given monitor if that current sample percentage is less than the target sample rate.
     *
     * @param monitor monitor instance
     * @return true if the monitor should be accepted in the sample
     */
    public boolean accept(Monitor monitor) {
        sampleRateLock.lock();
        boolean shouldAccept;
        float actualRate;

        try {
            if (populationCount == Integer.MAX_VALUE) {
                populationCount = 0;
                sampledCount = 0;
            }

            populationCount++;

            actualRate = (sampledCount / ((float)populationCount));
            shouldAccept = (actualRate < sampleRate);
            if (shouldAccept) {
                sampledCount++;
            }
        } finally {
            sampleRateLock.unlock();
        }

        if (logger.isDebugEnabled()) {
            logger.debug((shouldAccept ? "ACCEPTED" : "REJECTED") + " monitor \"" + monitor.get(Monitor.NAME) +
                    "\" due to actual sample rate of " + actualRate + " and target " + this.sampleRate);
        }

        return shouldAccept;
    }

    /**
     * Set sample rate (50.0f == 50% sample rate)
     *
     * @param samplePercentage sample rate as percentage
     */
    @ManagedAttribute(description="Set the sampling rate.  For e.g. a value of \"33.3\" will result in sampling every 3rd monitor")
    public void setSamplePercentage(float samplePercentage) {
        float previousRate = getSamplePercentage();
        setSamplePercentageInternal(samplePercentage);
        logger.info("Sampling percentage successfully changed from " + previousRate + " => " + samplePercentage);
    }

    @ManagedAttribute(description="Get the sampling rate as a percentage.")
    public float getSamplePercentage() {
        return this.sampleRate * 100.0f;
    }

    private void setSamplePercentageInternal(float samplePercentage) {
        validateSamplePercentage(samplePercentage);

        sampleRateLock.lock();        
        try {
            this.sampleRate = samplePercentage / 100.0f;
            this.populationCount = 0;
            this.sampledCount = 0;
        } finally {
            sampleRateLock.unlock();
        }
    }

    private void validateSamplePercentage(float samplePercentage) {
        if ((samplePercentage < 0.0f) || (samplePercentage > 100.0f)) {
            throw new IllegalArgumentException("samplePercentage must be between 0 and 100");
        }
    }
}
