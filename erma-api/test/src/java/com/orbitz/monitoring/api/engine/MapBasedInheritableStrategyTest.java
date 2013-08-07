package com.orbitz.monitoring.api.engine;

import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MapBasedInheritableStrategyTest extends TestCase {

    private InheritableStrategy strategy;

    protected void setUp() throws Exception {
        super.setUp();
        strategy = new MapBasedInheritableStrategy();

        MockMonitorProcessor _processor = new MockMonitorProcessor();
        MockMonitorProcessorFactory processorFactory =
                new MockMonitorProcessorFactory(
                        new MonitorProcessor[]{_processor});

        MockDecomposer _decomposer = new MockDecomposer();

        MonitoringEngine.getInstance().setProcessorFactory(processorFactory);
        MonitoringEngine.getInstance().setInheritableStrategy(strategy);
        MonitoringEngine.getInstance().setDecomposer(_decomposer);
        MonitoringEngine.getInstance().restart();
        MonitoringEngine.getInstance().setMonitoringEnabled(true);
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

    public void testChainedInheritableSerializedAttributes() {
        new TransactionMonitor("foo").setInheritable("foo", "bar").serializable();
        Map attributes = strategy.getInheritableAttributes();

        assertEquals(1, attributes.size());
        AttributeHolder attributeHolder = (AttributeHolder)attributes.values().toArray()[0];
        assertTrue(attributeHolder.isSerializable());
    }

    public void testInlineProvidedAttributes() {
        Map ermaAttributes = new HashMap();
        ermaAttributes.put("foo", "bar");

        new TransactionMonitor("blah", MonitoringLevel.ESSENTIAL, ermaAttributes);
    }

    protected void tearDown() throws Exception {
        strategy = null;
        MonitoringEngine.getInstance().clearCurrentThread();
        MonitoringEngine.getInstance().shutdown();
        super.tearDown();
    }
}
