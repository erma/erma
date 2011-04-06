package com.orbitz.monitoring.api.engine;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.Monitor;
import java.util.Map;
import junit.framework.TestCase;
import org.mockito.Mockito;

/**
 * Test case to ensure that {@link NoOpInheritableStrategy} provides the full
 * {@link InheritableStrategy} interface without throwing exceptions or interacting with
 * {@link Monitor} objects.
 */
public class NoOpInheritableStrategyTest extends TestCase {
    private CompositeMonitor monitor;
    private InheritableStrategy strategy;
    
    @Override
    protected void setUp() {
        this.strategy = new NoOpInheritableStrategy();
        this.monitor = Mockito.mock(CompositeMonitor.class);
    }
    
    /**
     * @see NoOpInheritableStrategy#clearCurrentThread()
     */
    public void testClearCurrentThread() {
        strategy.clearCurrentThread();
    }
    
    /**
     * @see NoOpInheritableStrategy#compositeMonitorCompleted(CompositeMonitor)
     */
    public void testCompositeMonitorCompleted() {
        strategy.compositeMonitorCompleted(monitor);
        Mockito.verifyZeroInteractions(monitor);
    }
    
    /**
     * @see NoOpInheritableStrategy#compositeMonitorStarted(CompositeMonitor)
     */
    public void testCompositeMonitorStarted() {
        strategy.compositeMonitorStarted(monitor);
        Mockito.verifyZeroInteractions(monitor);
    }
    
    /**
     * @see NoOpInheritableStrategy#getCompositeMonitorNamed(String)
     */
    public void testGetCompositeMonitorNamed() {
        assertNull(strategy.getCompositeMonitorNamed("name"));
    }
    
    /**
     * @see NoOpInheritableStrategy#getInheritableAttributes()
     */
    public void testGetInheritableAttributes() {
        final Map<?, ?> inhertableAttrs = strategy.getInheritableAttributes();
        assertNotNull(inhertableAttrs);
        assertTrue(inhertableAttrs.isEmpty());
    }
    
    /**
     * @see NoOpInheritableStrategy#processMonitorForCompositeMonitor(Monitor)
     */
    public void testProcessMonitorForCompositeMonitor() {
        strategy.processMonitorForCompositeMonitor(monitor);
        Mockito.verifyZeroInteractions(monitor);
    }
    
    /**
     * @see NoOpInheritableStrategy#setInheritable(CompositeMonitor, String,
     *      com.orbitz.monitoring.api.monitor.AttributeHolder)
     */
    public void testSetInheritable() {
        strategy.setInheritable(monitor, "name", null);
        Mockito.verifyZeroInteractions(monitor);
    }
    
    /**
     * @see NoOpInheritableStrategy#shutdown()
     */
    public void testShutdown() {
        strategy.shutdown();
    }
    
    /**
     * @see NoOpInheritableStrategy#startup()
     */
    public void testStartup() {
        strategy.startup();
    }
}
