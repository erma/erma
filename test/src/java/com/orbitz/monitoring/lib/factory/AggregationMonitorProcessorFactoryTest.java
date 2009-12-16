package com.orbitz.monitoring.lib.factory;

import junit.framework.TestCase;
import com.orbitz.monitoring.api.Aggregator;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.ValueMonitor;
import com.orbitz.monitoring.test.MockMonitorProcessor;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class AggregationMonitorProcessorFactoryTest extends TestCase {

    private MockAggregator aggregator;
    private MockMonitorProcessor processor;
    private AggregationMonitorProcessorFactory factory;

    protected void setUp() throws Exception {
        super.setUp();
        aggregator = new MockAggregator();
        processor = new MockMonitorProcessor();
        final ProcessGroup[] groups = new ProcessGroup[] {new ProcessGroup(processor)};
        Set<Class<? extends Monitor>> clazzes = new HashSet<Class<? extends Monitor>>();
        clazzes.add(ValueMonitor.class);
        factory = new AggregationMonitorProcessorFactory(groups, aggregator, clazzes);
    }

    public void testLifecycle() {
        assertFalse(aggregator.isRunning());
        factory.startup();
        assertTrue(aggregator.isRunning());
        factory.shutdown();
        assertFalse(aggregator.isRunning());
    }

    public void testNotRoutingToAggregation() {
        aggregator.setShouldAggregate(false);
        final Monitor m = new EventMonitor("should.not.aggregate");
        for(MonitorProcessor processor : factory.getProcessorsForMonitor(m)) {
            processor.process(m);
        }
        assertEquals(0, aggregator.getMonitors().size());
        final Monitor[] monitors = processor.extractProcessObjects();
        assertEquals(1, monitors.length);
        assertSame(m, monitors[0]);
    }

    public void testRoutingToAggregation() {
        final Monitor m = new ValueMonitor("should.aggregate", 1.0);
        for(MonitorProcessor processor : factory.getProcessorsForMonitor(m)) {
            processor.process(m);
        }
        assertEquals(0, processor.extractProcessObjects().length);
        final List<Monitor> monitors = aggregator.getMonitors();
        assertEquals(1, monitors.size());
        assertSame(m, monitors.get(0));
    }

    public void testRoutingToAggregationWithMonitoringLevel() {
        factory.setAggregationMonitoringLevel(MonitoringLevel.ESSENTIAL.toString());
        final Monitor m = new ValueMonitor("should.aggregate", 1.0);
        for(MonitorProcessor processor : factory.getProcessorsForMonitor(m)) {
            processor.process(m);
        }
        assertEquals(0, processor.extractProcessObjects().length);
        assertEquals(0, aggregator.getMonitors().size());
    }

    public void testRoutingToAggregationWithExpression() {
        factory.setAggregationExpression("name.equals('super.special')");
        final Monitor m = new ValueMonitor("should.aggregate", 1.0);
        for(MonitorProcessor processor : factory.getProcessorsForMonitor(m)) {
            processor.process(m);
        }
        assertEquals(0, processor.extractProcessObjects().length);
        assertEquals(0, aggregator.getMonitors().size());
    }

    protected void tearDown() throws Exception {
        factory = null;
        processor = null;
        aggregator = null;
        super.tearDown();
    }

    static class MockAggregator implements Aggregator {

        private boolean running = false;
        private boolean shouldAggregate = true;
        private List<Monitor> monitors = new LinkedList<Monitor>();

        public void aggregate(Monitor monitor) {
            monitors.add(monitor);
        }

        public List<Monitor> getMonitors() {
            final List<Monitor> copy = new ArrayList<Monitor>(monitors);
            monitors.clear();
            return copy;
        }

        public boolean isRunning() {
            return running;
        }

        public boolean shouldAggregate(Monitor monitor) {
            return shouldAggregate;
        }

        public void setShouldAggregate(boolean shouldAggregate) {
            this.shouldAggregate = shouldAggregate;
        }

        public void shutdown() {
            running = false;
        }

        public void startup() {
            running = true;
        }
    }
}
