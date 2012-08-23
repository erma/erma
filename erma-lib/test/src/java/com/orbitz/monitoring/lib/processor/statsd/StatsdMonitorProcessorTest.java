package com.orbitz.monitoring.lib.processor.statsd;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.ValueMonitor;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.lib.factory.ProcessGroup;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
import com.orbitz.statsd.StatsdClient;

public class StatsdMonitorProcessorTest extends TestCase {
    
    private StatsdClient statsdClient;
    private BaseMonitoringEngineManager manager;
    private StatsdMonitorProcessor statsdMonitorProcessor;
    
    public void setUp() throws Exception {
        MonitoringEngine.getInstance().shutdown();
        
        statsdClient = spy(new StatsdClient("localhost", 8125));
        statsdMonitorProcessor = new StatsdMonitorProcessor(statsdClient);
        ProcessGroup[] processGroups = new ProcessGroup[] { new ProcessGroup(statsdMonitorProcessor)};
        SimpleMonitorProcessorFactory processorFactory = new SimpleMonitorProcessorFactory(processGroups);
        manager = new BaseMonitoringEngineManager(processorFactory);
        manager.startup();
    }
    
    @Override
    protected void tearDown() throws Exception {
        manager.shutdown();
    }
    
    /**
     * Inconsequential method to simulate doing some work
     */
    private String doWork() {
        StringBuilder result = new StringBuilder()
            .append("doing")
            .append("some")
            .append("work");
        return result.toString();
    }
    
    public void testProcessTransactionMonitorSucceeded() throws Exception {
        TransactionMonitor monitor = new TransactionMonitor("time.some.work");
        try {
            doWork();
        } catch (Exception e) {
            monitor.failedDueTo(e);
        }
        monitor.done();
        
        verify(statsdClient).timing("time.some.work", monitor.getAsInt(Attribute.LATENCY));
    }

    public void testProcessTransactionMonitorFailed() throws Exception {
        TransactionMonitor monitor = new TransactionMonitor("time.some.work");
        try {
            doWork();
        } catch (Exception e) {
            monitor.failedDueTo(e);
        }
        monitor.failed();
        monitor.done();
        
        verify(statsdClient).timing("time.some.work", monitor.getAsInt(Attribute.LATENCY));
        verify(statsdClient).increment("time.some.work.failed");
    }
    
    public void testProcessValueMonitor() throws Exception {
        new ValueMonitor("measured.value", 42.1).fire();
        verify(statsdClient).gauge("measured.value", 42);
    }
    
    public void testProcessEventMonitor() throws Exception {
        new EventMonitor("something.happened").fire();
        verify(statsdClient).increment("something.happened");
    }
    
    public void testProcessFailedEventMonitor() throws Exception {
        EventMonitor monitor = new EventMonitor("something.bad.happened");
        monitor.set(Attribute.FAILED, true);
        monitor.fire();

        verify(statsdClient).increment("something.bad.happened");
        verify(statsdClient).increment("something.bad.happened.failed");
    }
    
    public void testExplicitlyNonFailedMonitor() throws Exception {
        EventMonitor monitor = new EventMonitor("something.bad.happened");
        monitor.set(Attribute.FAILED, false);
        monitor.fire();

        verify(statsdClient).increment("something.bad.happened");
        verify(statsdClient, never()).increment("something.bad.happened.failed");
    }

}
