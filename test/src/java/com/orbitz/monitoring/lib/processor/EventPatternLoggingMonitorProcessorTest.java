package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.lib.MonitoringEngineManager;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Unit tests for the EventPatternLoggingMonitorProcessor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class EventPatternLoggingMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private EventPatternLoggingMonitorProcessor _processor = new EventPatternLoggingMonitorProcessor();
    private String _testVMID = "JVM_ID_NOT_SET";

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        CPUProfilingMonitorProcessor cpuProfilingMonitorProcessor = new CPUProfilingMonitorProcessor();
        cpuProfilingMonitorProcessor.setEnabled(true);
        MockMonitorProcessorFactory mockMonitorProcessorFactory =
                new MockMonitorProcessorFactory(new MonitorProcessor[] {cpuProfilingMonitorProcessor});
        MonitoringEngineManager monitoringEngineManager =
                new MonitoringEngineManager(mockMonitorProcessorFactory);
        monitoringEngineManager.startup();
    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        MonitoringEngine.getInstance().shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testRenderEvent() {

        String name = "testEvent";
        EventMonitor event = new EventMonitor(name);
        event.fire();

        String expected = '\n'+_testVMID+'|'+name;
        String actual = _processor.renderMonitor(event);
        assertEquals("EventMonitor not rendered as expected", expected, actual);
    }

    public void testRenderTransaction() {
        String name = "testTransaction";
        TransactionMonitor txn = new TransactionMonitor(name);
        txn.succeeded();
        txn.done();

        String expected = '\n'+_testVMID+'|'+name;
        String actual = _processor.renderMonitor(txn);
        assertEquals("TransactionMonitor not rendered as expected", expected, actual);
    }

    public void testRenderTransactionContainingChildMonitors() {
        MonitoringEngine.getInstance().setEventPatternMonitoringLevel(MonitoringLevel.INFO);
        TransactionMonitor parent = new TransactionMonitor("parent");

        EventMonitor child = new EventMonitor("child1");
        child.fire();

        TransactionMonitor child2 = new TransactionMonitor("child2");
        child2.failedDueTo(new RuntimeException(new IllegalArgumentException()));
        child2.done();

        parent.succeeded();
        parent.done();

        String expected = '\n'+_testVMID+'|'+parent.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child2.get(Monitor.NAME)+"|java.lang.IllegalArgumentException";
        String actual = _processor.renderMonitor(parent);
        assertEquals("Transaction containing child Monitors not rendered as expected", expected, actual);
    }

    public void testRenderSerializedTransactionMonitor() {

        TransactionMonitor monitor = new TransactionMonitor("monitor");
        monitor.failedDueTo(new RuntimeException(new IllegalStateException("oops")));
        SerializableMonitor serializableMomento = monitor.getSerializableMomento();

        String expected = '\n'+_testVMID+'|'+monitor.get(Monitor.NAME)+"|java.lang.IllegalStateException";
        String actual = _processor.renderMonitor(serializableMomento);
        assertEquals("Serialized TransactionMonitor not rendered as expected", expected, actual);
    }

    public void testRenderTransactionWithBogusFailureThrowable() {

        TransactionMonitor parent = new TransactionMonitor("parent");

        EventMonitor child = new EventMonitor("child1");
        child.fire();

        TransactionMonitor child2 = new TransactionMonitor("child2");
        child2.set("failureThrowable", new Object());
        child2.done();

        parent.succeeded();
        parent.done();

        // should log a warning and append "failed"
        String expected = '\n'+_testVMID+'|'+parent.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child.get(Monitor.NAME)+
                "\n  "+_testVMID+'|'+child2.get(Monitor.NAME)+"|failed";
        String actual = _processor.renderMonitor(parent);
        assertEquals("Transaction containing child Monitors not rendered as expected", expected, actual);
    }

    public void testRenderTransactionWithDetail() {

        _processor.setIncludeCpuTime(true);
        _processor.setIncludeLatency(true);

        String name = "testTransaction";
        TransactionMonitor txn = new TransactionMonitor(name);
        txn.succeeded();
        txn.done();

        String expected = '\n'+_testVMID+'|'+name;
        String actual = _processor.renderMonitor(txn);
        assertTrue("TransactionMonitor not rendered as expected", actual.startsWith(expected));
        StringTokenizer tok = new StringTokenizer(actual, "|");
        tok.nextToken();
        tok.nextToken();
        String latency = tok.nextToken();
        Integer.parseInt(latency);
        String cpuTime = tok.nextToken();
        Float.parseFloat(cpuTime);
    }

    public void testMonitorsToSkip() {
        String name = "parentTransaction";
        TransactionMonitor txn = new TransactionMonitor(name);
        new EventMonitor("child1Monitor").fire();
        new EventMonitor("skipMonitor").fire();
        new EventMonitor("child2Monitor").fire();
        txn.succeeded();
        txn.done();

        Logger.getLogger("com.orbitz.monitoring").setLevel(Level.DEBUG);
        _processor.setMonitorsToSkip(Collections.singleton("skip"));

        String expected = '\n' + _testVMID + "|parentTransaction" +
                "\n  " + _testVMID + "|child1Monitor" +
                "\n  " + _testVMID + "|skipMonitor" +
                "\n  " + _testVMID + "|child2Monitor";
        String actual = _processor.renderMonitor(txn);
        assertEquals("Transaction containing child Monitors not rendered as expected", expected, actual);

        Logger.getLogger("com.orbitz.monitoring").setLevel(Level.INFO);

        expected = '\n' + _testVMID + "|parentTransaction" +
                "\n  " + _testVMID + "|child1Monitor" +
                "\n  " + _testVMID + "|child2Monitor";
        actual = _processor.renderMonitor(txn);
        assertEquals("Transaction containing child Monitors not rendered as expected", expected, actual);

        _processor.getMonitorsToSkip().clear();
        Logger.getLogger("com.orbitz.monitoring").setLevel(null);
    }

    public void testCondensingDuplicateMonitors() {
        String name = "parentTransaction";
        TransactionMonitor txn = new TransactionMonitor(name);
        new EventMonitor("childMonitor").fire();
        new EventMonitor("childMonitor").fire();
        new EventMonitor("otherMonitor").fire();
        txn.succeeded();
        txn.done();

        String expected = '\n' + _testVMID + "|parentTransaction" +
                "\n  " + _testVMID + "|childMonitor|2 occurences" +
                "\n  " + _testVMID + "|otherMonitor";
        String actual = _processor.renderMonitor(txn);
        assertEquals("Transaction containing child Monitors not rendered as expected", expected, actual);
    }

    public void testCondensingSubpattern() {
        String name = "parentTransaction";
        TransactionMonitor txn = new TransactionMonitor(name);
        TransactionMonitor foo = new TransactionMonitor("foo");
        new EventMonitor("childMonitor").fire();
        foo.succeeded();
        foo.done();
        foo = new TransactionMonitor("foo");
        new EventMonitor("childMonitor").fire();
        foo.succeeded();
        foo.done();
        new EventMonitor("otherMonitor").fire();
        txn.succeeded();
        txn.done();

        final String expected = '\n' + _testVMID + "|parentTransaction" +
                "\n  " + _testVMID + "|foo" + "|2 occurences" +
                "\n    " + _testVMID + "|childMonitor" +
                "\n  " + _testVMID + "|otherMonitor";

        final String actual = _processor.renderMonitor(txn);
        assertEquals("Transaction containing child Monitors not rendered as expected", expected, actual);
    }

    public void testCyclicExceptions() {
        // log4j
        ConsoleAppender c = new ConsoleAppender();
        c.setLayout(new PatternLayout("%p [%c] %m%n"));
        c.setTarget("System.out");
        c.setImmediateFlush(true);
        c.setName("Console");
        c.activateOptions();
        Logger.getRootLogger().addAppender(c);
        Level level = Logger.getRootLogger().getLevel();
        Logger.getRootLogger().setLevel(Level.WARN);

        // this should never happen in real code
        Exception e1 = new UnsupportedOperationException("foo");
        Exception e2 = new IllegalStateException("bar", e1);
        e1.initCause(e2);

        String name = "testTransaction";
        TransactionMonitor txn = new TransactionMonitor(name);
        txn.failedDueTo(e2);
        txn.done();

        final String expected = '\n' + _testVMID + "|" + name + "|java.lang.UnsupportedOperationException";
        
        final String actual = _processor.renderMonitor(txn);
        assertEquals("Transaction containing cyclic exceptionss not rendered as expected", expected, actual);

        SerializableCompositeMonitor sTxn = (SerializableCompositeMonitor) txn.getSerializableMomento();

        final String sActual = _processor.renderMonitor(sTxn);
        assertEquals("Transaction containing cyclic exceptionss not rendered as expected", expected, sActual);

        Logger.getRootLogger().removeAppender(c);
        Logger.getRootLogger().setLevel(level);
    }

}
