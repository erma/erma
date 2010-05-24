package com.orbitz.monitoring.lib.factory;

import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit test for SimpleMonitorProcessorFactory.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class SimpleMonitorProcessorFactoryTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private MockMonitorProcessor _a;
    private MockMonitorProcessor _b;
    private MockMonitorProcessor _c;

    private ProcessGroup _justA;
    private ProcessGroup _aAndB;
    private ProcessGroup _aAndC;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _a = new MockMonitorProcessor();
        _b = new MockMonitorProcessor();
        _c = new MockMonitorProcessor();

        _justA = new ProcessGroup(new MonitorProcessor[] {_a});
        _aAndB = new ProcessGroup(new MonitorProcessor[] {_a, _b});
        _aAndC = new ProcessGroup(new MonitorProcessor[] {_a, _c});
    }

    // ** TEST METHODS ********************************************************
    public void testStartup() {
        ProcessGroup[] processGroups = new ProcessGroup[] {_justA, _aAndB};

        SimpleMonitorProcessorFactory factory =
                new SimpleMonitorProcessorFactory(processGroups);

        SimpleMonitorProcessorFactory factory2 =
                new SimpleMonitorProcessorFactory(new ProcessGroup[] {_justA});
        factory.startup();

        _a.assertStartupCalled();
        _b.assertStartupCalled();
    }

    public void testShutdown() {
        ProcessGroup[] processGroups = new ProcessGroup[] {_justA, _aAndB};

        SimpleMonitorProcessorFactory factory =
                new SimpleMonitorProcessorFactory(processGroups);
        factory.startup();
        factory.shutdown();

        _a.assertShutdownCalled();
        _b.assertShutdownCalled();
    }

    public void testConstructionWithNullProcessGroup() {
        SimpleMonitorProcessorFactory factory =
                new SimpleMonitorProcessorFactory(null);
        factory.startup();
        factory.shutdown();
        MonitorProcessor[] processors = factory.getProcessorsForMonitor(
                new EventMonitor("foo"));

        assertNotNull("Processor[] is null", processors);
        assertEquals("Processor[].length", 0, processors.length);
    }

    public void testNoProcessGroupMatches() {
        ProcessGroup[] processGroups = new ProcessGroup[] {_justA};
        _justA.setExpression("m.name == 'baz'");

        SimpleMonitorProcessorFactory factory =
                new SimpleMonitorProcessorFactory(processGroups);
        factory.startup();

        MonitorProcessor[] processors = factory.getProcessorsForMonitor(
                new EventMonitor("bar"));

        assertNotNull("Processor[] is null", processors);
        assertEquals("Process[].length", 0, processors.length);
    }

    public void testProcessGroupMatches() {
        ProcessGroup[] processGroups = new ProcessGroup[] {_justA, _aAndB};
        _justA.setExpression("m.name == 'baz'");
        _aAndB.setExpression("m.name == 'xxx'");

        SimpleMonitorProcessorFactory factory =
                new SimpleMonitorProcessorFactory(processGroups);
        factory.startup();

        MonitorProcessor[] processors = factory.getProcessorsForMonitor(
                new EventMonitor("baz"));

        assertNotNull("Processor[] is null", processors);
        assertEquals("Processor[].length", 1, processors.length);
        assertSame("processor[0] == a", _a, processors[0]);
    }

    public void testSameMonitorInTwoMatchingProcessGroups() {
        ProcessGroup[] processGroups = new ProcessGroup[] {_aAndB, _aAndC};
        _aAndB.setExpression("m.name == 'baz'");
        _aAndC.setExpression("m.name == 'baz'");

        SimpleMonitorProcessorFactory factory =
                new SimpleMonitorProcessorFactory(processGroups);
        factory.startup();

        MonitorProcessor[] processors = factory.getProcessorsForMonitor(
                new EventMonitor("baz"));

        assertNotNull("Processor[] is null", processors);

        MonitorProcessor[] expectedMP = new MonitorProcessor[]{_a,_b,_c};
        List expected = Arrays.asList(expectedMP);
        List actual = Arrays.asList(processors);
        assertEquals("Processor[].size", expected.size(), actual.size());
        assertEquals("Processor[] contents", expected, actual);
    }

}
