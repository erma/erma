package com.orbitz.monitoring.api.engine;

import junit.framework.TestCase;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.util.Collections;

public class MapBasedInheritableStrategyTest extends TestCase {

    private InheritableStrategy strategy;

    protected void setUp() throws Exception {
        super.setUp();
        strategy = new MapBasedInheritableStrategy();
    }

    public void testClearCurrentThread() {
        assertEquals(0, strategy.clearCurrentThread());

        final TransactionMonitor m = new TransactionMonitor("test");
        strategy.compositeMonitorStarted(m);
        strategy.setInheritable(m, "foo", new AttributeHolder("bar"));
        assertEquals(1, strategy.clearCurrentThread());
    }

    public void testCompositeMonitorStartedAndCompleted() {
        final TransactionMonitor parent = new TransactionMonitor("parent");
        strategy.compositeMonitorStarted(parent);
        assertEquals(Collections.emptyMap(), strategy.getInheritableAttributes());

        strategy.setInheritable(parent, "foo", new AttributeHolder("bar"));
        assertEquals(Collections.singletonMap("foo", new AttributeHolder("bar")), strategy.getInheritableAttributes());

        final TransactionMonitor child = new TransactionMonitor("child");
        strategy.compositeMonitorStarted(child);
        strategy.setInheritable(child, "foo", new AttributeHolder("baz"));
        assertEquals(Collections.singletonMap("foo", new AttributeHolder("bar")), strategy.getInheritableAttributes());

        strategy.compositeMonitorCompleted(child);
        assertEquals(Collections.singletonMap("foo", new AttributeHolder("bar")), strategy.getInheritableAttributes());

        strategy.compositeMonitorCompleted(parent);
        assertEquals(Collections.emptyMap(), strategy.getInheritableAttributes());
    }

    public void testGetCompositeMonitorNamed() {
        try {
            strategy.getCompositeMonitorNamed("test");
            fail();
        } catch (Exception doNothing) { }
    }

    public void testLifecycle() {
        strategy.startup();
        strategy.compositeMonitorStarted(new TransactionMonitor("test"));
        strategy.shutdown();
        assertEquals(Collections.emptyMap(), strategy.getInheritableAttributes());
    }

    protected void tearDown() throws Exception {
        strategy = null;
        super.tearDown();
    }
}
