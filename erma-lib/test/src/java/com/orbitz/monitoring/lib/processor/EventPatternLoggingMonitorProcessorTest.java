package com.orbitz.monitoring.lib.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractCompositeMonitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;

/**
 * Unit tests for the EventPatternLoggingMonitorProcessor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class EventPatternLoggingMonitorProcessorTest extends TestCase {
    private String _testVMID = "JVM_ID_NOT_SET";
    private EventPatternLoggingMonitorProcessor processor;
    private TestAppender appender;

    protected void setUp() throws Exception {
        super.setUp();
        processor = new EventPatternLoggingMonitorProcessor();
        appender = new TestAppender();
        processor.startup();

        Logger logger = Logger.getLogger(EventPatternLoggingMonitorProcessor.class.getName());
        logger.addAppender(appender);
        logger.setLevel(Level.ALL);
    }
    
    public void testCreated() {
        String name = "testEvent";
        Monitor event = new AbstractMonitor(name) {};
        event.set(Monitor.VMID, _testVMID);

        processor.monitorCreated(event);
        
        assertEquals(0, appender.getEvents().size());
    }
    
    public void testStarted() {
        String name = "testEvent";
        Monitor event = new AbstractMonitor(name) {};
        event.set(Monitor.VMID, _testVMID);

        processor.monitorStarted(event);
        
        assertEquals(0, appender.getEvents().size());
    }
    
    public void testProcess() {
        List<String> attributes = new ArrayList<String>();
        attributes.add("vmid");
        attributes.add("name");
        attributes.add("latency");
        attributes.add("cpuTimeMillis");
        processor.setAllowedAttributes(attributes);
 
        String name = "testEvent";
        CompositeMonitor event = new AbstractCompositeMonitor(name) {};
        event.set(Monitor.VMID, _testVMID);
        event.set("latency", 1000);
        event.set("cpuTimeMillis", 1234);

        Monitor skip = new AbstractMonitor("skip") {};
        skip.set(Monitor.VMID, _testVMID);
        event.addChildMonitor(skip);

        String expected = '\n'+_testVMID+'|'+name + "|1000|1234";

        processor.setMonitorsToSkip(Collections.singleton("skip"));
        processor.process(event);
        
        assertEquals(1, appender.getEvents().size());
        LoggingEvent loggingEvent = appender.getEvents().get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals(expected, loggingEvent.getMessage());
    }

}
