package com.orbitz.monitoring.api.engine;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.MonitoringLevel;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StackBasedInheritableStrategyTest extends TestCase {

    private StackBasedInheritableStrategy strategy;

    protected void setUp() throws Exception {
        strategy = new StackBasedInheritableStrategy();
    }

    public void testCompositeMonitorCompletedBelowMonitoringLevel() {
        strategy.setEventPatternLevel(MonitoringLevel.INFO);
        CompositeMonitor infoMonitor = mock(CompositeMonitor.class);
        when(infoMonitor.getLevel()).thenReturn(MonitoringLevel.INFO);
        strategy.compositeMonitorStarted(infoMonitor);
        strategy.compositeMonitorCompleted(infoMonitor);

        CompositeMonitor debugMonitor = mock(CompositeMonitor.class);
        when(debugMonitor.getLevel()).thenReturn(MonitoringLevel.DEBUG);
        strategy.compositeMonitorStarted(debugMonitor);
        strategy.compositeMonitorCompleted(debugMonitor);
    }
}
