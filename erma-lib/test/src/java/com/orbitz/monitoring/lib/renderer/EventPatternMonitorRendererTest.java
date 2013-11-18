package com.orbitz.monitoring.lib.renderer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractCompositeMonitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;

public class EventPatternMonitorRendererTest {
    private static final List<String> DEFAULT_ATTRIBUTES = new ArrayList<String>();

    static {
        DEFAULT_ATTRIBUTES.add("vmid");
        DEFAULT_ATTRIBUTES.add("name");
        DEFAULT_ATTRIBUTES.add("failureThrowable");
    }
    
    private String _testVMID = "JVM_ID_NOT_SET";

    private EventPatternMonitorRenderer renderer;
    
    @Before
    public void setUp() {
        renderer = new EventPatternMonitorRenderer(DEFAULT_ATTRIBUTES);
    }
    
    @Test
    public void renderMonitor() {
        String name = "testEvent";
        Monitor event = new AbstractMonitor(name) {};
        event.set(Monitor.VMID, _testVMID);

        String expected = '\n'+_testVMID+'|'+name;
        String actual = renderer.renderMonitor(event);
        assertEquals("Monitor not rendered as expected", expected, actual);
    }

    @Test
    public void compositeMonitorContainingChildMonitors() {
        CompositeMonitor parent = new AbstractCompositeMonitor("parent"){};
        parent.set(Monitor.VMID, _testVMID);

        Monitor child = new AbstractMonitor("child1") {};
        child.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(child);

        Monitor child2 = new AbstractMonitor("child2") {};
        child2.set(Monitor.VMID, _testVMID);
        child2.set("failureThrowable", new RuntimeException(new IllegalArgumentException()));
        parent.addChildMonitor(child2);

        String expected = '\n'+_testVMID+'|'+parent.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child2.get(Monitor.NAME)+"|java.lang.IllegalArgumentException";
        String actual = renderer.renderMonitor(parent);
        assertEquals("Monitor containing child Monitors not rendered as expected", expected, actual);
    }
    
    @Test
    public void bogusFailureThrowable() {
        Monitor monitor = new AbstractMonitor("monitor") {};
        monitor.set(Monitor.VMID, _testVMID);
        monitor.set("failureThrowable", new Object());

        // should log a warning and append "failed"
        String expected = '\n'+_testVMID+'|'+monitor.get(Monitor.NAME)+"|failed";
        String actual = renderer.renderMonitor(monitor);
        assertEquals("Monitor with bad throwable failed to render as expected", expected, actual);
    }

    @Test
    public void renderTransactionWithDetail() {
        List<String> attributes = new ArrayList<String>();
        attributes.add("vmid");
        attributes.add("name");
        attributes.add("latency");
        attributes.add("cpuTimeMillis");
        renderer.setAllowedAttributes(attributes);
        
        String name = "testTransaction";
        Monitor txn = new AbstractMonitor(name) {};
        txn.set(Monitor.VMID, _testVMID);
        txn.set("latency", 1000);
        txn.set("cpuTimeMillis", 1234);

        String expected = '\n' + _testVMID + '|' + name + "|1000|1234";
        String actual = renderer.renderMonitor(txn);
        assertEquals("TransactionMonitor not rendered as expected", expected, actual);
    }

    @Test
    public void monitorsCombined() {
        CompositeMonitor parent = new AbstractCompositeMonitor("parent"){};
        parent.set(Monitor.VMID, _testVMID);

        Monitor child = new AbstractMonitor("child") {};
        child.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(child);

        Monitor child2 = new AbstractMonitor("child") {};
        child2.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(child2);

        Monitor otherChild = new AbstractMonitor("otherChild") {};
        otherChild.set(Monitor.VMID, _testVMID);
        otherChild.set("failureThrowable", new RuntimeException(new IllegalArgumentException()));
        parent.addChildMonitor(otherChild);

        String expected = '\n'+_testVMID+'|'+parent.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child.get(Monitor.NAME)+"|2 occurences"+
                "\n  "+_testVMID+'|'+otherChild.get(Monitor.NAME)+"|java.lang.IllegalArgumentException";
        String actual = renderer.renderMonitor(parent);
        assertEquals("Monitor containing child Monitors not rendered as expected", expected, actual);
    }


    @Test
    public void monitorsToSkip() {
        CompositeMonitor parent = new AbstractCompositeMonitor("parent"){};
        parent.set(Monitor.VMID, _testVMID);

        Monitor child = new AbstractMonitor("child") {};
        child.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(child);

        Monitor skip = new AbstractMonitor("skip") {};
        skip.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(skip);

        Monitor otherChild = new AbstractMonitor("otherChild") {};
        otherChild.set(Monitor.VMID, _testVMID);
        otherChild.set("failureThrowable", new RuntimeException(new IllegalArgumentException()));
        parent.addChildMonitor(otherChild);

        Monitor skipOther = new AbstractMonitor("skipOther") {};
        skipOther.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(skipOther);

        renderer.setMonitorsToSkip(Collections.singleton("skip"));

        String expected = '\n'+_testVMID+'|'+parent.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+otherChild.get(Monitor.NAME)+"|java.lang.IllegalArgumentException";
        String actual = renderer.renderMonitor(parent);
        assertEquals("Monitor containing child Monitors not rendered as expected", expected, actual);
    }
    
    @Test
    public void skipLast() {
        CompositeMonitor parent = new AbstractCompositeMonitor("parent"){};
        parent.set(Monitor.VMID, _testVMID);

        Monitor skip = new AbstractMonitor("skip") {};
        skip.set(Monitor.VMID, _testVMID);
        parent.addChildMonitor(skip);

        renderer.setMonitorsToSkip(Collections.singleton("skip"));

        String expected = '\n'+_testVMID+'|'+parent.get(Monitor.NAME);
        String actual = renderer.renderMonitor(parent);
        assertEquals("Monitor containing child Monitors not rendered as expected", expected, actual);
    }

}
