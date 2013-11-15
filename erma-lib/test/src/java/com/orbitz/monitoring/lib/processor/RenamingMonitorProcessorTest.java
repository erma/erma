package com.orbitz.monitoring.lib.processor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.monitor.AbstractCompositeMonitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;

public class RenamingMonitorProcessorTest extends TestCase {

    private MonitorProcessor _renamingMonitorProcessor;
    private MonitorProcessor _delegate;
    private ArgumentCaptor<Monitor> _monitorCaptor;

    @Override
    protected void setUp() throws Exception {        
        _monitorCaptor = ArgumentCaptor.forClass(Monitor.class);
        _delegate = mock(MonitorProcessor.class);
        
        String[] attributes = new String[] {"foo", "bar", "notThere"};
        _renamingMonitorProcessor = new RenamingMonitorProcessor(_delegate, (List<String>) Arrays.asList(attributes));

        // Clean up before going into test case.
        reset(_delegate);
    }
    
    public void testSimpleMonitorCreated() {
        Monitor m = new AbstractMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");
        
        _renamingMonitorProcessor.monitorCreated(m);

        verify(_delegate).monitorCreated(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }

    public void testSimpleMonitorStarted() {
        Monitor m = new AbstractMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");

        _renamingMonitorProcessor.monitorStarted(m);

        verify(_delegate).monitorStarted(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }

    public void testSimpleMonitorProcessed() {
        Monitor m = new AbstractMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");

        _renamingMonitorProcessor.process(m);

        verify(_delegate).process(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }
    
    public void testCompositeMonitorWithoutChildrenCreated() {
        Monitor m = new AbstractCompositeMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");
        
        _renamingMonitorProcessor.monitorCreated(m);

        verify(_delegate).monitorCreated(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }

    public void testCompositeMonitorWithoutChildrenStarted() {
        Monitor m = new AbstractCompositeMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");

        _renamingMonitorProcessor.monitorStarted(m);

        verify(_delegate).monitorStarted(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }

    public void testCompositeMonitorWithoutChildrenProcessed() {
        Monitor m = new AbstractCompositeMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");

        _renamingMonitorProcessor.process(m);

        verify(_delegate).process(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }
    
    public void testCompositeMonitorWithChildrenCreated() {
        CompositeMonitor m = new AbstractCompositeMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");
        
        Monitor c1 = new AbstractMonitor("childOne") {};
        m.addChildMonitor(c1);
        Monitor c2 = new AbstractMonitor("childTwo") {};
        m.addChildMonitor(c2);
        
        _renamingMonitorProcessor.monitorCreated(m);

        verify(_delegate).monitorCreated(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }

    public void testCompositeMonitorWithChildrenStarted() {
        CompositeMonitor m = new AbstractCompositeMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");
        
        Monitor c1 = new AbstractMonitor("childOne") {};
        m.addChildMonitor(c1);
        Monitor c2 = new AbstractMonitor("childTwo") {};
        m.addChildMonitor(c2);

        _renamingMonitorProcessor.monitorStarted(m);

        verify(_delegate).monitorStarted(_monitorCaptor.capture());
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(m, _monitorCaptor.getValue());
    }

    public void testCompositeMonitorWithChildrenProcessed() {
        CompositeMonitor m = new AbstractCompositeMonitor("something.happened") {};
        m.set("foo", "string1");
        m.set("bar", "string2");
        
        Monitor c1 = new AbstractMonitor("childOne") {};
        m.addChildMonitor(c1);
        Monitor c2 = new AbstractMonitor("childTwo") {};
        m.addChildMonitor(c2);

        _renamingMonitorProcessor.process(m);

        verify(_delegate).process(_monitorCaptor.capture());
        
        assertEquals("string1.string2..something.happened", _monitorCaptor.getValue().getAsString(Attribute.NAME));
        assertNonNameAttributesAndChildrenEqual(c1, _monitorCaptor.getValue());
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
