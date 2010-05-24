package com.orbitz.monitoring.api.monitor;

import com.clarkware.junitperf.ConstantTimer;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TimedTest;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.engine.MapBasedInheritableStrategy;
import com.orbitz.monitoring.api.engine.StackBasedInheritableStrategy;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockDecomposer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Load test for the Transaction monitor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class TransactionMonitorLoadTest extends TestCase {
    // ** CONSTRUCTORS ********************************************************
    public TransactionMonitorLoadTest() {
        super();
    }
    
    public TransactionMonitorLoadTest(String string) {
        super(string);
    }

    // ** TEST SUITE METHODS **************************************************
    public static Test suite() {
        MonitoringEngine mEngine = MonitoringEngine.getInstance();
        mEngine.setDecomposer(new MockDecomposer());
        setFactory(mEngine);
        mEngine.setInheritableStrategy(new StackBasedInheritableStrategy());
        mEngine.startup();

        TestSuite suite = new TestSuite();

        int iterations = 100;

        Test simpleTransactionUsage = new TransactionMonitorLoadTest(
                "testSimpleTransactionUsage");
        simpleTransactionUsage = new LoadTest(simpleTransactionUsage, 10, iterations);
        // I'm doing a TimedTest around the LoadTest because each individual test's
        // time can vary too wildly to get meaningful numbers. This variation is due
        // to GC pauses, solor winds, etc. Therefore, we're going to do the math here 
        // to figure out how long it should take to get the desired average run time.
        // In this case, we're shooting for <= 10ms per monitor, which is higher than
        // typical timings to account for any unforseen issues.
        //simpleTransactionUsage = new TimedTest(simpleTransactionUsage, 10 * iterations * 2);
        //((TimedTest)simpleTransactionUsage).setQuiet();

        suite.addTest(simpleTransactionUsage);

        return suite;
    }

    // ** TEST METHODS ********************************************************
    public void testSimpleTransactionUsage() {
        TransactionMonitor parent = new TransactionMonitor("parentLoad");
        parent.setInheritable("foo", "foo");
        parent.setInheritable("bar", "bar");

        TransactionMonitor child = new TransactionMonitor("childLoad");
        child.setInheritable("baz", "baz");
        child.succeeded();
        child.done();

        parent.succeeded();
        parent.done();
    }

    // ** PROTECTED METHODS ****************************************************
    protected static void setFactory(MonitoringEngine mEngine) {
        mEngine.setProcessorFactory(
                new MockMonitorProcessorFactory(new MonitorProcessor[0]));
    }
}
