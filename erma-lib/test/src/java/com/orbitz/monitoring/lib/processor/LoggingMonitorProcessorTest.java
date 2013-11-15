package com.orbitz.monitoring.lib.processor;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.lib.renderer.MonitorRenderer;

/**
 * Unit tests for the LoggingMonitorProcessor.
 * @author Operations Architecture
 */
public class LoggingMonitorProcessorTest extends TestCase {
    private final class SingleMonitorRenderer implements MonitorRenderer {
        
        Monitor singleMonitor;
        
        SingleMonitorRenderer(Monitor m) {
            singleMonitor = m;
        }
        
        @Override
        public String renderMonitor(Monitor monitor) {
            fail("Other render method should be called.");
            return null;
        }

        @Override
        public String renderMonitor(Monitor monitor, boolean includeStackTraces) {
            assertSame(singleMonitor, monitor);
            return Boolean.toString(includeStackTraces);
        }
    }

    private LoggingMonitorProcessor processor;
    private TestAppender appender;

    protected void setUp() throws Exception {
        super.setUp();
        processor = new LoggingMonitorProcessor();
        appender = new TestAppender();
        processor.startup();

        // log4j will not allow the same appender to be added multiple times
        //LogManager.resetConfiguration();
        Logger logger = Logger.getLogger(LoggingMonitorProcessor.class.getName());
        logger.addAppender(appender);
        logger.setLevel(Level.ALL);
    }
    
    public void testCreateOff() {
        Monitor m = new AbstractMonitor(){};
        processor.setLogMonitorCreated(false);

        processor.monitorCreated(m);
        
        assertTrue(appender.getEvents().isEmpty());
    }
    
    public void testCreateOn() {
        Monitor m = new AbstractMonitor(){};
        processor.setLogMonitorCreated(true);
        processor.setMonitorRenderer(new SingleMonitorRenderer(m));
        
        processor.monitorCreated(m);
        
        assertEquals(1, appender.getEvents().size());
        LoggingEvent loggingEvent = appender.getEvents().get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("monitorCreated: false", loggingEvent.getMessage());
    }
    
    public void testStartOff() {
        Monitor m = new AbstractMonitor(){};
        processor.setLogMonitorStarted(false);

        processor.monitorStarted(m);
        
        assertTrue(appender.getEvents().isEmpty());
    }
    
    public void testStartOn() {
        Monitor m = new AbstractMonitor(){};
        processor.setLogMonitorStarted(true);
        processor.setMonitorRenderer(new SingleMonitorRenderer(m));
        
        processor.monitorStarted(m);
        
        assertEquals(1, appender.getEvents().size());
        LoggingEvent loggingEvent = appender.getEvents().get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("monitorStarted: false", loggingEvent.getMessage());
    }
    
    public void testProcessOff() {
        Monitor m = new AbstractMonitor(){};
        processor.setLogProcess(false);

        processor.process(m);
        
        assertTrue(appender.getEvents().isEmpty());
    }
    
    public void testProcessOn() {
        Monitor m = new AbstractMonitor(){};
        processor.setLogProcess(true);
        processor.setMonitorRenderer(new SingleMonitorRenderer(m));
        
        processor.process(m);
        
        assertEquals(1, appender.getEvents().size());
        LoggingEvent loggingEvent = appender.getEvents().get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("process: false", loggingEvent.getMessage());
    }
}
