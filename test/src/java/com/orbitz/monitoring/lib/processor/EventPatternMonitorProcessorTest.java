package com.orbitz.monitoring.lib.processor;

import junit.framework.TestCase;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.MonitoringEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Test cases for EventPatternMonitorProcessor.
 * <p/>
 * <p>(c) 2000-07 Orbitz, LLC. All Rights Reserved.</p>
 *
 * @author Operations Architecture
 */
public class EventPatternMonitorProcessorTest extends TestCase {
    public void setUp() {
        // Shutting down the MonitoringEngine so because the vmid
        // attributes is set an locked, which causes these tests to
        // fail.
        MonitoringEngine.getInstance().shutdown();
    }

    public void testDefaultAttributes() {
        EventPatternMonitorProcessor processor = new EventPatternMonitorProcessor();

        TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.set("vmid", "vm");
        monitor.set("failed", "true");
        monitor.set("foo", "bar");

        processor.process(monitor);

        assertEquals("<TransactionMonitor name=\"name\" vmid=\"vm\" failed=\"true\"/>",
                monitor.get("eventPatternXml"));
    }

    public void testCustomAttributes() {
        List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("foo");
        EventPatternMonitorProcessor processor = new EventPatternMonitorProcessor();
        processor.setAllowedAttributes(allowedAttributes);

        TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.set("vmid", "vm");
        monitor.set("failed", "true");
        monitor.set("foo", "bar");

        processor.process(monitor);

        assertEquals("<TransactionMonitor name=\"name\" foo=\"bar\"/>",
                monitor.get("eventPatternXml"));
    }

    public void testEnableDisable() {
        EventPatternMonitorProcessor processor = new EventPatternMonitorProcessor();

        TransactionMonitor monitor = new TransactionMonitor("name");

        processor.setEnabled(false);
        processor.process(monitor);

        assertFalse(monitor.hasAttribute("eventPatternXml"));

        processor.setEnabled(true);
        assertTrue(processor.isEnabled());
        processor.process(monitor);

        assertEquals("<TransactionMonitor name=\"name\" failed=\"true\"/>",
                monitor.get("eventPatternXml"));
    }

    public void testListXmlAttributes() {
        EventPatternMonitorProcessor processor = new EventPatternMonitorProcessor();

        assertEquals("[name, vmid, failed, failureThrowable, latency]", processor.listXmlAttributes());
    }

    public void testAddRemoveAttributes() {
        EventPatternMonitorProcessor processor = new EventPatternMonitorProcessor();

        TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.set("x", "value");

        processor.addXmlAttribute("x");
        processor.process(monitor);

        assertEquals("<TransactionMonitor name=\"name\" failed=\"true\" x=\"value\"/>",
                monitor.get("eventPatternXml"));

        processor.removeXmlAttribute("x");
        processor.process(monitor);

        assertEquals("<TransactionMonitor name=\"name\" failed=\"true\"/>",
                monitor.get("eventPatternXml"));

        processor.removeXmlAttribute("name");
        processor.removeXmlAttribute("vmid");
        processor.removeXmlAttribute("failed");
        processor.removeXmlAttribute("failureThrowable");
        processor.removeXmlAttribute("latency");

        processor.process(monitor);

        assertEquals("<TransactionMonitor/>",
                monitor.get("eventPatternXml"));

        processor.removeXmlAttribute("foo");        
    }

    public void testMaxPatternSize() {
        EventPatternMonitorProcessor processor = new EventPatternMonitorProcessor();
        assertEquals("Default max event pattern size not as expected.", 50000, processor.getMaxPatternSize());
        processor.setMaxPatternSize(20000);
        assertEquals("Max event pattern size not as expected.", 20000, processor.getMaxPatternSize());
    }

}
