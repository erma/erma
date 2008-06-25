package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.test.CompositeMonitorTestBase;
import com.orbitz.monitoring.test.MockMonitorProcessor;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

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
}
