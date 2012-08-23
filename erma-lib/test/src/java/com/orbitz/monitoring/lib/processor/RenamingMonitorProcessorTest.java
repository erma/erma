package com.orbitz.monitoring.lib.processor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.ValueMonitor;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.lib.factory.ProcessGroup;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;

public class RenamingMonitorProcessorTest extends TestCase {

    private MonitorProcessor _renamingMonitorProcessor;
    private BaseMonitoringEngineManager _manager;
    private MonitorProcessor _delegate;
    private ArgumentCaptor<Monitor> _monitorCaptor;

    @Override
    protected void setUp() throws Exception {
        MonitoringEngine.getInstance().shutdown();
        
        _monitorCaptor = ArgumentCaptor.forClass(Monitor.class);
        _delegate = mock(MonitorProcessor.class);
        
        String[] attributes = new String[] {"environment", Attribute.VMID, Attribute.HOSTNAME, "instance"};
        _renamingMonitorProcessor = new RenamingMonitorProcessor(_delegate, (List<String>) Arrays.asList(attributes));
        
        ProcessGroup[] processGroups = new ProcessGroup[] { new ProcessGroup(_renamingMonitorProcessor)};
        SimpleMonitorProcessorFactory processorFactory = new SimpleMonitorProcessorFactory(processGroups);
        _manager = new BaseMonitoringEngineManager(processorFactory);
        
        MonitoringEngine.getInstance().setGlobalAttribute("environment", "test");
        MonitoringEngine.getInstance().setGlobalAttribute(Attribute.VMID, "lib-monitoring-tests");
        MonitoringEngine.getInstance().setGlobalAttribute(Attribute.HOSTNAME, "example_org");
        MonitoringEngine.getInstance().setGlobalAttribute("instance", 0);
        
        _manager.startup();
        
        // MonitoringEngine startup created some monitors that got processed.
        // Clean up before going into test case.
        reset(_delegate);
    }
    
    @Override
    protected void tearDown() throws Exception {
        _manager.shutdown();
    }

    public void testProcessEventMonitor() throws Exception {
        EventMonitor monitor = new EventMonitor("something.happened");
        monitor.fire();
        
        verify(_delegate).monitorCreated(_monitorCaptor.capture());
        assertEquals("test.lib-monitoring-tests.example_org.0.something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(monitor, _monitorCaptor.getValue());
        
        verify(_delegate).process(_monitorCaptor.capture());
        assertEquals("test.lib-monitoring-tests.example_org.0.something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(monitor, _monitorCaptor.getValue());
    }

    public void testProcessTransactionMonitor() throws Exception {
        TransactionMonitor monitor = new TransactionMonitor("do.some.work");
        
        verify(_delegate).monitorCreated(_monitorCaptor.capture());
        assertEquals("test.lib-monitoring-tests.example_org.0.do.some.work", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        
        verify(_delegate).monitorStarted(_monitorCaptor.capture());
        assertEquals("test.lib-monitoring-tests.example_org.0.do.some.work", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(monitor, _monitorCaptor.getValue());
        
        monitor.succeeded();
        monitor.done();
        
        verify(_delegate).process(_monitorCaptor.capture());
        assertEquals("test.lib-monitoring-tests.example_org.0.do.some.work", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(monitor, _monitorCaptor.getValue());
    }
    
    public void testProcessTransactionMonitorWithChildMonitors() throws Exception {
        TransactionMonitor monitor = new TransactionMonitor("do.some.work");
        
        EventMonitor event = new EventMonitor("event");
        event.fire();
        
        ValueMonitor value = new ValueMonitor("value", 20);
        value.fire();
        
        monitor.succeeded();
        monitor.done();
        
        verify(_delegate, times(3)).process(_monitorCaptor.capture());
        List<Monitor> captured = _monitorCaptor.getAllValues();
        
        assertEquals("test.lib-monitoring-tests.example_org.0.event", captured.get(0).getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(event, captured.get(0));
        assertEquals("test.lib-monitoring-tests.example_org.0.value", captured.get(1).getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(value, captured.get(1));
        assertEquals("test.lib-monitoring-tests.example_org.0.do.some.work", captured.get(2).getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(monitor, captured.get(2));
    }
    
    private void assertNonNameAttributesAndChildrenEqual(Monitor originalMonitor, Monitor handledMonitor) {
        Set<String> keySet = originalMonitor.getAll().keySet();
        for (Object key : keySet) {
            if (!Attribute.NAME.equals(key)) {
                assertTrue(handledMonitor.hasAttribute((String) key));
                assertEquals(originalMonitor.getAll().get(key), handledMonitor.get((String) key));
            }
        }
        if (CompositeMonitor.class.isAssignableFrom(originalMonitor.getClass())) {
            Collection<Monitor> originalChildren = ((CompositeMonitor) originalMonitor).getChildMonitors();
            // handledMonitor should cast to CompositeMonitor if originalMonitor was CompositeMonitor
            Collection<Monitor> handledChildren = ((CompositeMonitor) handledMonitor).getChildMonitors();
            assertTrue(originalChildren.containsAll(handledChildren));
            assertTrue(handledChildren.containsAll(originalChildren));
        }
    }
    
}
