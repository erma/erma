package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.lib.factory.ProcessGroup;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the AsyncMonitorProcessor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class AsyncMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private AsyncMonitorProcessor _processor;
    private MockMonitorProcessor _attachedProcessor;
    private BaseMonitoringEngineManager _monitoringEngineManager;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _attachedProcessor = new MockMonitorProcessor();
        _processor = new AsyncMonitorProcessor(new MonitorProcessor[]{_attachedProcessor});

        ProcessGroup processGroup = new ProcessGroup(_processor);
        SimpleMonitorProcessorFactory simpleMonitorProcessorFactory = new SimpleMonitorProcessorFactory(new ProcessGroup[]{processGroup});

        _monitoringEngineManager = new BaseMonitoringEngineManager(simpleMonitorProcessorFactory);
        _monitoringEngineManager.startup();

        _processor.startup();
    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        _processor.shutdown();
        _monitoringEngineManager.shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testMonitorCreated() {
        EventMonitor event = new EventMonitor("test");

        _processor.monitorCreated(event);
        _processor.flushEvents();
        Monitor[] monitors = _attachedProcessor.extractMonitorCreatedObjects();
        List<Monitor> monitorList =  Arrays.asList(monitors);
        boolean checkMonitorCreated = false;
        for(Monitor monitor : monitors) {
            if (monitor.get(Monitor.NAME).equals(event.get(Monitor.NAME))) {
              assertEquals(event.get(Monitor.CREATED_AT) , monitor.get(Monitor.CREATED_AT));
              assertEquals(event.get(Monitor.THREAD_ID) , monitor.get(Monitor.THREAD_ID));
              checkMonitorCreated = true;
           }
        }
       assertTrue(checkMonitorCreated);
    }

    public void testProcess() {
        EventMonitor event = new EventMonitor("test");

        _processor.process(event);
        _processor.flushEvents();

        Monitor[] monitors = _attachedProcessor.extractProcessObjects();
        List<Monitor> monitorList =  Arrays.asList(monitors);
        boolean checkMonitorCreated = false;
        for(Monitor monitor : monitors) {
            if (monitor.get(Monitor.NAME).equals(event.get(Monitor.NAME))) {
              assertEquals(event.get(Monitor.CREATED_AT) , monitor.get(Monitor.CREATED_AT));
              assertEquals(event.get(Monitor.THREAD_ID) , monitor.get(Monitor.THREAD_ID));
              checkMonitorCreated = true;
           }
        }
       assertTrue(checkMonitorCreated);
    }
}
