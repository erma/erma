package com.orbitz.monitoring.api;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.api.engine.StackBasedInheritableStrategy;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * Unit tests for the {@link MonitoringEngine}.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class MonitoringEngineTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private MonitoringEngine _engine;
    private MockMonitorProcessorFactory _factory;
    private MockMonitorProcessor _processor;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty("orbitz.props", "/dev/null");

        _processor = new MockMonitorProcessor();

        _factory = new MockMonitorProcessorFactory(
                new MonitorProcessor[]{_processor});

        _engine = MonitoringEngine.getInstance();
        _engine.setProcessorFactory(_factory);
        _engine.setDecomposer(new MockDecomposer());
        _engine.setInheritableStrategy(new StackBasedInheritableStrategy());
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        _engine.shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testMontoringDefaultEnabledSetting() {
        _engine.startup();
        assertTrue("For engine enabled", _engine.isEnabled());
    }

    public void testCallingStartupWithoutMonitorStrategySet() {
        _engine.setInheritableStrategy(null);

        try {
            _engine.startup();
            fail("Expected IllegalStateException when monitorStrategy is null at startup");
        } catch (Exception e) {
            // Expected
        }

        // This shouldn't error out because the engine didn't successfully
        // startup
        _engine.shutdown();
    }

    public void testStartupWithoutProcessorFactorySet() {
        _engine.setProcessorFactory(null);

        try {
            _engine.startup();
            fail("Expected IllegalStateException when MPF is null at startup");
        } catch (IllegalStateException e) {
            // Expected
        }

        // This shouldn't error out because the engine didn't successfully
        // startup
        _engine.shutdown();
    }

    public void testStartupWithoutDecomposerSet() {
        _engine.setDecomposer(null);

        try {
            _engine.startup();
            fail("Expected IllegalStateException when decomposer is null at startup");
        } catch (IllegalStateException e) {
            // Expected
        }

        // This shouldn't error out because the engine didn't successfully
        // startup
        _engine.shutdown();
    }

    public void testStartupCallsFactoryStartup() {
        _engine.startup();

        _factory.assertStartupCalled();
    }

    public void testFactoryThrowsExceptionOnStartup() {
        _factory.setThrowExceptionOnStartup(true);

        // The engine should let startup exceptions through to the client code
        // in case they want to handle it
        try {
            _engine.startup();
        } catch (Exception e) {
            // Expected
            _factory.assertStartupCalled();
            return;
        }

        fail("Exception should have been thrown");
    }

    public void testShutdownCallsFactoryShutdown() {
        _engine.startup();
        _engine.shutdown();

        _factory.assertShutdownCalled();
    }

    public void testFactoryThrowsExceptionOnShutdown() {
        _engine.startup();

        _factory.setThrowExceptionOnShutdown(true);

        // The engine should catch all expceptions or should it notify the
        // person shutting down the engine that all is not well?
        try {
            _engine.shutdown();
        } catch (Exception e) {
            // Expected an exception to propagate
            _factory.assertShutdownCalled();
            return;
        }

        fail("Exception should have been thrown");
    }

    public void testFactoryThrowsThrowableOnGetProcessorsForMonitor() {
        _engine.startup();

        _factory.setThrowThrowableOnGetProcessors(true);

        // The engine should ensure that throwables are not thrown from any of
        // the monitor handling methods. Failure to do so will cause the client
        // code to fail due to a monitoring error.
        try {
            TransactionMonitor txn = new TransactionMonitor("test");
            _engine.monitorCreated(txn);
            _engine.compositeMonitorStarted(txn);
            _engine.compositeMonitorCompleted(txn);
            _engine.process(txn);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("All Throwables should be caught by the engine");
        }
    }

    public void testProcessorThrowsThrowableDuringProcessingAMonitor() {
        _engine.startup();

        _processor.setThrowThrowableDuringProcessing(true);

        // The engine should ensure that throwables are not thrown from any of
        // the monitor handling methods. Failure to do so will cause the client
        // code to fail due to a monitoring error.
        try {
            TransactionMonitor txn = new TransactionMonitor("test");
            _engine.monitorCreated(txn);
            _engine.compositeMonitorStarted(txn);
            _engine.compositeMonitorCompleted(txn);
            _engine.process(txn);
        } catch (Throwable t) {
            fail("All Throwables should be caught by the engine");
        }
    }

    public void testOneProcessorThrowingThrowableDoesntAffectOtherProcessors() {
        MockMonitorProcessor otherProcessor = new MockMonitorProcessor();

        _factory = new MockMonitorProcessorFactory(
                new MonitorProcessor[]{_processor, otherProcessor});
        _engine.setProcessorFactory(_factory);

        _processor.setThrowThrowableDuringProcessing(true);

        _engine.startup();

        // The engine should ensure that otherProcessor receives all the events
        // even though _processor is throwing Throwables left and right.
        try {
            TransactionMonitor txn = new TransactionMonitor("test");
            _engine.monitorCreated(txn);
            otherProcessor.assertExpectedMonitorCreatedObject(txn);

            _engine.compositeMonitorStarted(txn);
            _engine.compositeMonitorCompleted(txn);

            _engine.process(txn);
            otherProcessor.assertExpectedProcessObject(txn);

            otherProcessor.assertNoUnexpectedCalls();
        } catch (Throwable t) {
            fail("All Exceptions should be caught by the engine");
        }
    }

    public void testProcessingWithoutStartupDoesntThrowException() {
        try {
            TransactionMonitor txn = new TransactionMonitor("test");
            _engine.monitorCreated(txn);
            _engine.compositeMonitorStarted(txn);
            _engine.compositeMonitorCompleted(txn);
            _engine.process(txn);

            _processor.assertNoUnexpectedCalls();
        } catch (Exception e) {
            fail("All Exceptions should be caught by the engine");
        }
    }

    public void testChildMonitorAttributeInheritance() {
        _engine.startup();

        TransactionMonitor parentMonitor = new TransactionMonitor("parent");

        parentMonitor.setInheritable("inheritable", "foo");
        parentMonitor.set("notInheritable", "bar");

        TransactionMonitor child1Monitor = new TransactionMonitor("child1");

        EventMonitor child2Monitor = new EventMonitor("child2");
        child2Monitor.fire();

        child1Monitor.succeeded();
        child1Monitor.done();

        parentMonitor.setInheritable("postInherit", "baz");
        parentMonitor.succeeded();
        parentMonitor.done();

        assertEquals("foo", child1Monitor.get("inheritable"));
        assertEquals("child2", child2Monitor.get(Monitor.NAME));
        assertEquals("foo", child2Monitor.get("inheritable"));
        assertFalse(child2Monitor.hasAttribute("notInheritable"));
        assertFalse(child2Monitor.hasAttribute("postInherit"));
        assertEquals("m", child1Monitor.get("parentSequenceId"));
        assertEquals("m_0", child1Monitor.get("sequenceId"));
        assertEquals("m_0", child2Monitor.get("parentSequenceId"));
        assertEquals("m_0_0", child2Monitor.get("sequenceId"));

        assertEquals("parent", parentMonitor.get(Monitor.NAME));
        assertEquals("foo", parentMonitor.get("inheritable"));
        assertEquals("bar", parentMonitor.get("notInheritable"));
        assertEquals("baz", parentMonitor.get("postInherit"));
        assertEquals("m", parentMonitor.get("sequenceId"));
        assertFalse(parentMonitor.hasAttribute("parentSequenceId"));
    }

    public void testMultithreadedChildMonitorCorrelation()
            throws InterruptedException {
        _engine.startup();

        CompositeMonitorUsage thread2Runnable = new CompositeMonitorUsage();
        Thread thread2 = new Thread(thread2Runnable, "thread");

        CompositeMonitorUsage thread1Runnable =
                new CompositeMonitorUsage(thread2);
        Thread thread1 = new Thread(thread1Runnable, "thread");


        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            fail("Error waiting for threads to complete");
        }


        Collection childMonitors1 = thread1Runnable._monitor.getChildMonitors();
        assertEquals(1, childMonitors1.size());
        assertEquals("child", ((Monitor) childMonitors1.iterator().next())
                .get("name"));

        Collection childMonitors2 = thread2Runnable._monitor.getChildMonitors();
        assertEquals(1, childMonitors2.size());
        assertEquals("child", ((Monitor) childMonitors2.iterator().next())
                .get("name"));
    }

    public void testGetCompositeMonitorNamed() {
        _engine.startup();

        TransactionMonitor monitor = new TransactionMonitor("test");

        assertSame("For monitor reference", monitor,
                   _engine.getCompositeMonitorNamed("test"));

        monitor.done();

        assertNull("For monitor reference",
                   _engine.getCompositeMonitorNamed("test"));
    }

    public void testGetCompositeMonitorNamedWithInstrumentationErrors() {
        _engine.startup();

        TransactionMonitor parent = new TransactionMonitor("parent");

        TransactionMonitor child = new TransactionMonitor("child");
        // child not correctly instrumented

        assertSame("For parent reference", parent,
                   _engine.getCompositeMonitorNamed("parent"));

        parent.done();
    }

    public void testGetCompositeMonitorNamedReturnsFirstFound() {
        _engine.startup();

        TransactionMonitor monitor1 = new TransactionMonitor("test");
        TransactionMonitor monitor2 = new TransactionMonitor("test");

        assertSame("For test monitor", monitor2,
                   _engine.getCompositeMonitorNamed("test"));

        monitor2.done();

        assertSame("For test monitor", monitor1,
                   _engine.getCompositeMonitorNamed("test"));
    }

    public void testGetCompositeMonitorNamedNullName() {
        _engine.startup();

        try {
            _engine.getCompositeMonitorNamed(null);
            fail("Exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testGlobalAttributes() {
        _engine.startup();

        _engine.setGlobalAttribute("object", "foo");
        _engine.setGlobalAttribute("short", (short) 1);
        _engine.setGlobalAttribute("char", (char) 1);
        _engine.setGlobalAttribute("int", 1);
        _engine.setGlobalAttribute("long", (long) 1);
        _engine.setGlobalAttribute("float", (float) 1.0);
        _engine.setGlobalAttribute("double", 1.0);
        _engine.setGlobalAttribute("boolean", true);

        EventMonitor monitor = new EventMonitor("test");
        assertEquals("foo", monitor.get("object"));
        assertEquals(1, monitor.getAsShort("short"));
        assertEquals(1, monitor.getAsChar("char"));
        assertEquals(1, monitor.getAsInt("int"));
        assertEquals(1, monitor.getAsLong("long"));
        assertEquals(1, monitor.getAsFloat("float"), 0);
        assertEquals(1, monitor.getAsDouble("double"), 0);
        assertEquals(true, monitor.getAsBoolean("boolean"));
        
    }

    public void testClearCompositeMonitorRefs() {
        _engine.startup();
        int count = _engine.clearCurrentThread();
        assertEquals(count, 0);

        TransactionMonitor m = new TransactionMonitor("foo");
        m.succeeded();
        m.done();
        count = _engine.clearCurrentThread();
        assertEquals(count, 0);

        m = new TransactionMonitor("foo");
        count = _engine.clearCurrentThread();
        assertEquals(count, 1);

        m = new TransactionMonitor("foo");
        TransactionMonitor m2 = new TransactionMonitor("foo2");
        count = _engine.clearCurrentThread();
        assertEquals(count, 2);

        count = _engine.clearCurrentThread();
        assertEquals(count, 0);
    }

    public void testMakeSerializableWithoutStartupDoesntThrowException() {
        try {
            TransactionMonitor txn = new TransactionMonitor("test");
            txn.set("foo", "bar");
            _engine.shutdown();
            SerializableMonitor momento = txn.getSerializableMomento();
            _engine.startup();

            _processor.assertNoUnexpectedCalls();
        } catch (Exception e) {
            fail("All Exceptions should be caught by the engine");
        }
    }

    public void testSerializableAttributes() {
        _engine = new MonitoringEngine();
        _engine.setProcessorFactory(_factory);
        _engine.setDecomposer(new MockDecomposer());
        _engine.setInheritableStrategy(new StackBasedInheritableStrategy());
        _engine.startup();

        Map attributeHolders = new HashMap();

        EventMonitor m = new EventMonitor("event");
        attributeHolders.put("foo",m.set("foo", "bar"));
        attributeHolders.put("baz",m.set("baz", "notSerialized").notSerializable());

        Map decomposed = _engine.makeAttributeHoldersSerializable(attributeHolders);
        assertEquals("decomposed string should equal monitor attributes", m.getAsString("foo"), ((AttributeHolder)decomposed.get("foo")).getValue());
    }

    public void testMonitoringLevels() {
        _engine = new MonitoringEngine();
        _engine.setProcessorFactory(_factory);
        _engine.setDecomposer(new MockDecomposer());
        _engine.setInheritableStrategy(new StackBasedInheritableStrategy());
        _engine.startup();

        try {
            _engine.addMonitorLevel(null, MonitoringLevel.INFO);
            fail("adding a level for a null monitor name should fail fast");
        } catch (NullPointerException e) {}

        try {
            _engine.addProcessorLevel(null, MonitoringLevel.INFO);
            fail("adding a level for a null processor name should fail fast");
        } catch (NullPointerException e) {}

        _engine.addMonitorLevel("foo", MonitoringLevel.DEBUG);
        _engine.addProcessorLevel("bar", MonitoringLevel.DEBUG);

        EventMonitor m = new EventMonitor("foo.bar.MyMonitor");
        MonitoringLevel updatedLevel = _engine.getOverrideLevelForMonitor(m);
        assertEquals("Updated level should be used", MonitoringLevel.DEBUG, updatedLevel);

        _engine.addMonitorLevel("foo.bar", MonitoringLevel.ESSENTIAL);
        assertEquals("Most specific monitor level should be used", MonitoringLevel.ESSENTIAL,
                _engine.getOverrideLevelForMonitor(m));

        _engine.addMonitorLevel("foo.bar.MyMonitor", MonitoringLevel.DEBUG);
        assertEquals("Most specific monitor level should be used", MonitoringLevel.DEBUG,
                _engine.getOverrideLevelForMonitor(m));

        EventMonitor m1 = new EventMonitor("foo");
        updatedLevel = _engine.getOverrideLevelForMonitor(m1);
        assertEquals("Updated level should be used", MonitoringLevel.DEBUG, updatedLevel);

        EventMonitor m2 = new EventMonitor("baz");
        updatedLevel = _engine.getOverrideLevelForMonitor(m2);
        assertNull("construction time level should be used, so the updated level would be null", updatedLevel);
    }

    // ** INNER CLASSES *******************************************************
    private static class CompositeMonitorUsage implements Runnable {
        public TransactionMonitor _monitor;
        private Thread _waitThread;

        public CompositeMonitorUsage() {
            this(null);
        }

        public CompositeMonitorUsage(Thread waitThread) {
            _waitThread = waitThread;
        }

        public void run() {
            _monitor = new TransactionMonitor("parent");

            TransactionMonitor child = new TransactionMonitor("child");
            child.done();

            try {
                if (_waitThread != null) {
                    _waitThread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            _monitor.done();
        }
    }
}
