package com.orbitz.monitoring.api.engine;

import java.util.Map;

import junit.framework.TestCase;

import org.mockito.Mockito;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.engine.NullInheritableStrategy;

/**
 * Test case to ensure that {@link NullInheritableStrategy} provides the full
 * {@link InheritableStrategy} interface without throwing exceptions or
 * interacting with {@link Monitor} objects.
 */
public class NullInheritableStrategyTest extends TestCase {

    private InheritableStrategy strategy;
    private CompositeMonitor monitor;

    @Override
    protected void setUp() {
        this.strategy = new NullInheritableStrategy();
        this.monitor = Mockito.mock(CompositeMonitor.class);
    }

    public void testClearCurrentThread() {
        strategy.clearCurrentThread();
    }

    public void testCompositeMonitorCompleted() {
        strategy.compositeMonitorCompleted(monitor);
        Mockito.verifyZeroInteractions(monitor);
    }

    public void testCompositeMonitorStarted() {
        strategy.compositeMonitorStarted(monitor);
        Mockito.verifyZeroInteractions(monitor);
    }

    public void testGetCompositeMonitorNamed() {
        assertNull(strategy.getCompositeMonitorNamed("name"));
    }

    public void testGetInheritableAttributes() {
        Map<?, ?> inhertableAttrs = strategy.getInheritableAttributes();
        assertNotNull(inhertableAttrs);
        assertTrue(inhertableAttrs.isEmpty());
    }

    public void testProcessMonitorForCompositeMonitor() {
        strategy.processMonitorForCompositeMonitor(monitor);
        Mockito.verifyZeroInteractions(monitor);
    }

    public void testSetInheritable() {
        strategy.setInheritable(monitor, "name", null);
        Mockito.verifyZeroInteractions(monitor);
    }

    public void testShutdown() {
        strategy.shutdown();
    }

    public void testStartup() {
        strategy.startup();
    }

}
