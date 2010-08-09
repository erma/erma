package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Test cases for PercentageMonitorSampler
 *
 * @author Greg Opaczewski
 */
public class PercentageMonitorSamplerTest {

    @Test
    public void testSampleRate() {
        Monitor monitor = new EventMonitor("test");

        MonitorSampler sampler = new PercentageMonitorSampler(100.0f);

        for (int i=0; i < 100; i++) {
            assertTrue(sampler.accept(monitor));
        }

        sampler = new PercentageMonitorSampler(50.0f);

        for (int i=0; i < 100; i++) {
            assertEquals((i % 2 == 0), sampler.accept(monitor));
        }

        sampler = new PercentageMonitorSampler(33.3f);

        for (int i=0; i < 100; i++) {
            assertEquals((i % 3 == 0), sampler.accept(monitor));
        }

        sampler = new PercentageMonitorSampler(0f);

        for (int i=0; i < 100; i++) {
            assertFalse(sampler.accept(monitor));
        }
    }

    @Test
    public void testSetSampleRate() {
        PercentageMonitorSampler sampler = new PercentageMonitorSampler(100.0f);

        float newRate = 0.0f;
        sampler.setSamplePercentage(newRate);
        assertEquals(newRate, sampler.getSamplePercentage());

        newRate = 20.0f;
        sampler.setSamplePercentage(newRate);
        assertEquals(newRate, sampler.getSamplePercentage());

        newRate = 100.0f;
        sampler.setSamplePercentage(newRate);
        assertEquals(newRate, sampler.getSamplePercentage());

        try {
            sampler.setSamplePercentage(-0.1f);
            fail("should not be able to set a negative sample rate");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            sampler.setSamplePercentage(110f);
            fail("should not be able to set a sample rate greater than 100");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
