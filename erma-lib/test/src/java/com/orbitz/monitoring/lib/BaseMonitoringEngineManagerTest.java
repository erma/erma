package com.orbitz.monitoring.lib;

import junit.framework.TestCase;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.engine.StackBasedInheritableStrategy;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.lib.renderer.XmlMonitorRenderer;

import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for the BaseMonitoringEngineManager.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class BaseMonitoringEngineManagerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************


    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {

        super.setUp();
    }

    protected void tearDown()
            throws Exception {

        super.tearDown();
    }

    // ** TEST METHODS ********************************************************
    public void testBaseMonitoringEngineManager() {

        BaseMonitoringEngineManager manager = new BaseMonitoringEngineManager();

        manager.startup();
        
        manager.reload();

        manager.shutdown();
    }

    public void testMaliciousRuntimeControls() {
        BaseMonitoringEngineManager manager = new BaseMonitoringEngineManager();
        manager.startup();

        int i = 0;
        try {
            while (true) {
                //prefixing the name with "zz" so it won't interfere with other level tests
                manager.updateLevelForMonitor("zz" + Integer.toHexString(i++), "DEBUG");
            }
        } catch (RuntimeException e) {
            // expected
        } catch (OutOfMemoryError e) {
            fail("RuntimeException should have been thrown");
        }
    }

    public void testEPMLevels() {

        List attributeList = new ArrayList();
        attributeList.add("name");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(attributeList);
        renderer.setPrettyPrint(true);

        BaseMonitoringEngineManager manager = new BaseMonitoringEngineManager();
        manager.startup();

        {
        TransactionMonitor txn = new TransactionMonitor("foo");

        EventMonitor m1 = new EventMonitor("bar");
        m1.fire();

        EventMonitor m2 = new EventMonitor("baz");
        m2.fire();

        txn.done();

        assertEquals("<TransactionMonitor name=\"foo\">\n" +
                "  <EventMonitor name=\"bar\"/>\n" +
                "  <EventMonitor name=\"baz\"/>\n" +
                "</TransactionMonitor>", renderer.renderMonitor(txn));
        }

        StackBasedInheritableStrategy.class.cast(MonitoringEngine.getInstance().getInheritableStrategy()).setEventPatternLevel(MonitoringLevel.ESSENTIAL);

        {
        TransactionMonitor txn = new TransactionMonitor("foo", MonitoringLevel.ESSENTIAL);

        EventMonitor m1 = new EventMonitor("bar");
        m1.fire();

        EventMonitor m2 = new EventMonitor("baz");
        m2.fire();

        txn.done();

        assertEquals("<TransactionMonitor name=\"foo\"/>", renderer.renderMonitor(txn));
        }

    }
}
