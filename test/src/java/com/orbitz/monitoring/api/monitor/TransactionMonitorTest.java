package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.test.CompositeMonitorTestBase;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unit tests for the {@link TransactionMonitor} monitor object.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class TransactionMonitorTest extends CompositeMonitorTestBase {
    // ** TEST METHODS ********************************************************
    public void testTransaction()
            throws Exception {
        TransactionMonitor transaction = new TransactionMonitor("fakeTxn");
        assertConstructionInvariants(transaction);

        Thread.sleep(100);

        transaction.succeeded();
        transaction.done();
        assertTransactionFinishedInvariants(transaction);
    }

    public void testFailedTransaction()
            throws Exception {
        TransactionMonitor transaction = new TransactionMonitor("fakeTxn");
        assertConstructionInvariants(transaction);

        Thread.sleep(100);

        transaction.failed();
        transaction.done();
        assertTransactionFinishedInvariants(transaction);
    }

    public void testFailedTransactionWithException()
            throws Exception {
        TransactionMonitor transaction = new TransactionMonitor("fakeTxn");
        assertConstructionInvariants(transaction);

        Thread.sleep(100);

        Exception e = new Exception();
        transaction.failedDueTo(e);
        transaction.done();

        assertTransactionFinishedInvariants(transaction);
        assertSame("For transaction failure exception", e,
                   transaction.get(TransactionMonitor.FAILURE_THROWABLE));
    }

    public void testChildMonitorPopulation() {
        TransactionMonitor parentTransaction = new TransactionMonitor("parentTransaction");

        TransactionMonitor childTransaction = new TransactionMonitor("child1");
        childTransaction.succeeded();
        childTransaction.done();

        EventMonitor childEvent = new EventMonitor("child2");
        childEvent.fire();

        parentTransaction.succeeded();
        parentTransaction.done();

        new TransactionMonitor("unrelated").done();

        assertNotNull("For parent's children monitors",
                      parentTransaction.getChildMonitors());
        Iterator childMonitors = parentTransaction.getChildMonitors().iterator();
        assertSame("For child 1", childTransaction, childMonitors.next());
        assertSame("For child 2", childEvent, childMonitors.next());
        assertFalse("More children", childMonitors.hasNext());
    }

    public void testChildTransactionMissedEndCall() {
        TransactionMonitor parent = new TransactionMonitor("parent");
        MockMonitorProcessor processor = getMockProcessor(parent);

        TransactionMonitor child = new TransactionMonitor("child");
        TransactionMonitor childsChild = new TransactionMonitor("childsChild");

        processor.clear(); // Don't care about create or started messages

        parent.succeeded();
        parent.done();

        processor.assertExpectedProcessObject(childsChild);
        processor.assertExpectedProcessObject(child);
        processor.assertExpectedProcessObject(parent);
        processor.assertNoUnexpectedCalls();

        new TransactionMonitor("unrelated").done();

        assertNotNull("For child's children", child.getChildMonitors());
        Iterator childMonitors = child.getChildMonitors().iterator();
        assertSame("For child", childsChild, childMonitors.next());
        assertFalse("More children", childMonitors.hasNext());

        assertNotNull("For parent's children", parent.getChildMonitors());
        childMonitors = parent.getChildMonitors().iterator();
        assertSame("For child", child, childMonitors.next());
        assertFalse("More children", childMonitors.hasNext());
    }

    public void testSettingInheritableAttributeAsNull() {
        TransactionMonitor mon = new TransactionMonitor("test");

        mon.setInheritable("key1", "a");

        Map inheritableAttributes = mon.getInheritableAttributes();
        assertTrue("Inheritable key1",
                    inheritableAttributes.containsKey("key1"));
    }

    public void testFailedByDefault() {
        TransactionMonitor mon = new TransactionMonitor("test");
        mon.done();
        assertTrue("For mon.failed", mon.getAsBoolean(
                TransactionMonitor.FAILED));

        mon = new TransactionMonitor("test", new HashMap());
        mon.done();
        assertTrue("For mon.failed", mon.getAsBoolean(
                TransactionMonitor.FAILED));

        mon = new TransactionMonitor(this.getClass(), "test");
        mon.done();
        assertTrue("For mon.failed", mon.getAsBoolean(
                TransactionMonitor.FAILED));

        mon = new TransactionMonitor(this.getClass(), "test", new HashMap());
        mon.done();
        assertTrue("For mon.failed", mon.getAsBoolean(
                TransactionMonitor.FAILED));
    }

    public void testChildCallsDoneTwice() {
        TransactionMonitor monitor = new TransactionMonitor("foo");

        TransactionMonitor child = new TransactionMonitor("bar");
        child.done();
        child.done();

        monitor.done();
    }

    public void testBadNameTransaction() {
        TransactionMonitor txn = new TransactionMonitor("fake|Txn,");

        assertEquals("invalid chars should be removed from name", "fakeTxn", txn.get(Monitor.NAME));

        txn.done();

        getMockProcessor(txn).assertExpectedProcessObject(txn);
    }

    public void testInheritableAttributes() {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Map inheritableAttributes = new HashMap();
        inheritableAttributes.put("requestId", 0);
        inheritableAttributes.put("attribute", "test");

        AbstractRunnable baz = new WorkerRunnable("Baz");
        AbstractRunnable bar = new DelegatingRunnable("Bar", executor, baz);
        AbstractRunnable foo = new DelegatingRunnable("Foo", inheritableAttributes, executor, bar);

        Future future = executor.submit(foo);
        try {
            future.get(50, TimeUnit.MILLISECONDS);
        } catch (Exception doNothing) {
            // no-op
        }

        assertInheritableAttributes("Foo", foo.monitors);
        assertInheritableAttributes("Bar", bar.monitors);
        assertInheritableAttributes("Baz", baz.monitors);

        executor.shutdownNow();
    }

    // ** PROTECTED METHODS ***************************************************
    protected Monitor createMonitor(String name, Map inheritedAttributes) {
        return new TransactionMonitor(name, inheritedAttributes);
    }

    protected Monitor[] useMonitors() {
        TransactionMonitor[] transactions = new TransactionMonitor[3];

        transactions[0] = new TransactionMonitor("success");
        transactions[0].succeeded();
        transactions[0].done();

        transactions[1] = new TransactionMonitor("failed");
        transactions[1].failed();
        transactions[1].done();

        transactions[2] = new TransactionMonitor("failedWithException");
        transactions[2].failedDueTo(new Exception());
        transactions[2].done();

        return transactions;
    }

    protected void completeMonitorUse(Monitor monitor) {
        ((TransactionMonitor) monitor).done();
    }

    // ** PRIVATE METHODS *****************************************************
    private void assertConstructionInvariants(TransactionMonitor transaction) {
        MockMonitorProcessor processor = getMockProcessor(transaction);
        processor.assertExpectedMonitorCreatedObject(transaction);
        assertNotNull("For transaction startTime",
                      transaction.get(TransactionMonitor.START_TIME));
    }

    private void assertTransactionFinishedInvariants(TransactionMonitor transaction) {
        MockMonitorProcessor processor = getMockProcessor(transaction);
        processor.assertExpectedProcessObject(transaction);
        processor.assertNoUnexpectedCalls();

        assertTrue("For latency", transaction.getAsLong(
                TransactionMonitor.LATENCY) > 0);
    }

    private void assertInheritableAttributes(String appName, Map<String,Monitor> monitors) {
        if(monitors.containsKey("in")) {
            Monitor m = monitors.get("in");
            assertEquals(appName, m.getAsString("appName"));
            assertEquals(0, m.getAsInt("requestId"));
            assertEquals("test", m.getAsString("attribute"));
        }
        if(monitors.containsKey("work")) {
            Monitor m = monitors.get("work");
            assertEquals(appName, m.getAsString("appName"));
            assertEquals(0, m.getAsInt("requestId"));
            assertEquals("test", m.getAsString("attribute"));
        }
        if(monitors.containsKey("out")) {
            Monitor m = monitors.get("out");
            assertEquals(appName, m.getAsString("appName"));
            assertEquals(0, m.getAsInt("requestId"));
            assertEquals("test", m.getAsString("attribute"));
        }
    }

    // ** PRIVATE CLASSES *****************************************************
    abstract class AbstractRunnable implements Runnable {

        protected String appName;
        protected Map inheritableAttributes;
        protected Map<String,Monitor> monitors;

        public AbstractRunnable(String appName, Map inheritableAttributes) {
            super();
            this.appName = appName;
            this.inheritableAttributes = inheritableAttributes;
            this.monitors = new ConcurrentHashMap<String,Monitor>();
        }

    }

    class DelegatingRunnable extends AbstractRunnable {

        private ExecutorService executor;
        private AbstractRunnable delegate;

        public DelegatingRunnable(String appName, ExecutorService executor, AbstractRunnable delegate) {
            this(appName, null, executor, delegate);
        }

        public DelegatingRunnable(String appName, Map inheritableAttributes, ExecutorService executor, AbstractRunnable delegate) {
            super(appName, inheritableAttributes);
            this.executor = executor;
            this.delegate = delegate;
        }

        public void run() {
            if(inheritableAttributes != null) {
                inheritableAttributes.put("appName", appName);
            }
            TransactionMonitor in = new TransactionMonitor("threadIn_" + appName, inheritableAttributes);
            try {
                TransactionMonitor work = new TransactionMonitor(appName + ".execute");
                try {
                    TransactionMonitor out = new TransactionMonitor("threadOut_" + delegate.appName);
                    try {
                        delegate.inheritableAttributes = MonitoringEngine.getInstance().getInheritableAttributes();
                        Future future = executor.submit(delegate);
                        future.get(50, TimeUnit.MILLISECONDS);
                    } catch (Exception doNothing) {
                        // no-op
                    } finally {
                        out.done();
                        monitors.put("out", out);
                    }
                } finally {
                    work.done();
                    monitors.put("work", work);
                }
            } finally {
                in.done();
                monitors.put("in", in);
                MonitoringEngine.getInstance().clearCurrentThread();
            }
        }
    }

    class WorkerRunnable extends AbstractRunnable {
        public WorkerRunnable(String appName) {
            super(appName, null);
        }

        public void run() {
            if(inheritableAttributes != null) {
                inheritableAttributes.put("appName", appName);
            }
            TransactionMonitor in = new TransactionMonitor("threadIn_" + appName, inheritableAttributes);
            try {
                TransactionMonitor work = new TransactionMonitor(appName + ".execute");
                work.done();
                monitors.put("work", work);
            } finally {
                in.done();
                monitors.put("in", in);
                MonitoringEngine.getInstance().clearCurrentThread();
            }
        }
    }
}
