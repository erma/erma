package com.orbitz.monitoring.api.monitor;

import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TimedTest;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
//import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
//import com.orbitz.monitoring.lib.factory.ProcessGroup;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Load test for Event usage.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class EventMonitorLoadTest extends TestCase {
    
    // ** CONSTRUCTORS ********************************************************
    public EventMonitorLoadTest(String string) {
        super(string);
    }

    // ** TEST SUITE METHODS **************************************************
    public static Test suite() {
        MonitoringEngine mEngine = MonitoringEngine.getInstance();
        mEngine.setProcessorFactory(
                new MockMonitorProcessorFactory(new MonitorProcessor[0]));
        mEngine.startup();

        int iterations = 10000;

        Test eventTest = new EventMonitorLoadTest("testEventUsage");
        eventTest = new LoadTest(eventTest, 10, iterations);
        // I'm doing a TimedTest around the LoadTest because each individual test's
        // time can vary too wildly to get meaningful numbers. This variation is due
        // to GC pauses, solor winds, etc. Therefore, we're going to do the math here 
        // to figure out how long it should take to get the desired average run time.
        // In this case, we're shooting for <= 10 ms per monitor, which is higher
        // than expected production times to account for any unforseen slow downs.
        eventTest = new TimedTest(eventTest, 10 * iterations);
        //((TimedTest) eventTest).setQuiet();

        return eventTest;
    }

    // ** TEST METHODS ********************************************************
    public void testEventUsage()
            throws Exception {
        EventMonitor event = new EventMonitor("testEvent");
        event.set("foo", "bar");
        event.fire();
    }
}
