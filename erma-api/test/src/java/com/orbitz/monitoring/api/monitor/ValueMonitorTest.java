package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockDecomposer;

import junit.framework.TestCase;

import java.util.Date;

public class ValueMonitorTest extends TestCase {

    private MockMonitorProcessor _processor;
    private MockDecomposer _decomposer;

    protected void setUp()
            throws Exception {
        _processor = new MockMonitorProcessor();
        MockMonitorProcessorFactory processorFactory =
                new MockMonitorProcessorFactory(
                        new MonitorProcessor[]{_processor});

        _decomposer = new MockDecomposer();

        MonitoringEngine mEngine = MonitoringEngine.getInstance();
        mEngine.setProcessorFactory(processorFactory);
        mEngine.setDecomposer(_decomposer);
        mEngine.restart();
        mEngine.setMonitoringEnabled(true);

        mEngine.setGlobal(Attribute.VMID, "VMID").lock();
    }

    protected void tearDown()
            throws Exception {
        MonitoringEngine.getInstance().shutdown();
    }

    public void testNonInheritingConstructors() {
        TransactionMonitor tm = new TransactionMonitor("myTM");
        tm.setInheritable("inheritable", "value");

        ValueMonitor vm = new ValueMonitor("foo", 0.0d);
        // all ValueMonitors should have these attributes after construction:
        assertEquals("foo", vm.get(Attribute.NAME));
        assertEquals(0.0d, vm.get(Attribute.VALUE));
        assertEquals(MonitoringLevel.INFO, vm.getLevel());
        assertTrue(vm.hasAttribute(Attribute.THREAD_ID));
        assertTrue(vm.hasAttribute(Attribute.CREATED_AT));
        // ensure global attributes are set:
        assertEquals("VMID", vm.get(Attribute.VMID));
        // non inheriting ValueMonitors should NOT have these attributes:
        assertFalse(vm.hasAttribute(Attribute.PARENT_SEQUENCE_ID));
        assertFalse(vm.hasAttribute(Attribute.SEQUENCE_ID));
        assertFalse(vm.hasAttribute("inheritable"));

        vm.fire();

        ValueMonitor vm2 = new ValueMonitor("bar", 1.0d, MonitoringLevel.DEBUG);
        assertEquals("bar", vm2.get(Attribute.NAME));
        assertEquals(1.0d, vm2.get(Attribute.VALUE));
        assertEquals(MonitoringLevel.DEBUG, vm2.getLevel());
        assertTrue(vm2.hasAttribute(Attribute.THREAD_ID));
        assertTrue(vm2.hasAttribute(Attribute.CREATED_AT));
        assertEquals("VMID", vm2.get(Attribute.VMID));
        assertFalse(vm2.hasAttribute(Attribute.PARENT_SEQUENCE_ID));
        assertFalse(vm2.hasAttribute(Attribute.SEQUENCE_ID));
        assertFalse(vm2.hasAttribute("inheritable"));

        vm2.fire();
                
        tm.done();

        // the ValueMonitor does not invoke the "monitorCreated" lifecycle event
        Monitor[] monitors = _processor.extractMonitorCreatedObjects();
        assertEquals(1, monitors.length);
        assertEquals(tm, monitors[0]);
        // all 3 monitors should invoke the "process" lifecycle event
        monitors = _processor.extractProcessObjects();
        assertEquals(3, monitors.length);
        assertEquals(vm, monitors[0]);
        assertEquals(vm2, monitors[1]);
        assertEquals(tm, monitors[2]);
    }

    public void testInheritingConstructors() {
        TransactionMonitor tm = new TransactionMonitor("myTM");
        tm.setInheritable("inheritable", "value");

        ValueMonitor vm = new ValueMonitor("foo", 0.0d, true);
        // all ValueMonitors should have these attributes after construction:
        assertEquals("foo", vm.get(Attribute.NAME));
        assertEquals(0.0d, vm.get("value"));
        assertEquals(MonitoringLevel.INFO, vm.getLevel());
        assertTrue(vm.hasAttribute(Attribute.THREAD_ID));
        assertTrue(vm.hasAttribute(Attribute.CREATED_AT));
        // ensure global attributes are set:
        assertEquals("VMID", vm.get(Attribute.VMID));
        // inheriting ValueMonitors SHOULD have these attributes:
        assertTrue(vm.hasAttribute(Attribute.PARENT_SEQUENCE_ID));
        assertTrue(vm.hasAttribute(Attribute.SEQUENCE_ID));
        assertTrue(vm.hasAttribute("inheritable"));

        vm.fire();

        ValueMonitor vm2 = new ValueMonitor("bar", 1.0d, true, MonitoringLevel.ESSENTIAL);
        assertEquals("bar", vm2.get(Attribute.NAME));
        assertEquals(1.0d, vm2.get(Attribute.VALUE));
        assertEquals(MonitoringLevel.ESSENTIAL, vm2.getLevel());
        assertTrue(vm2.hasAttribute(Attribute.THREAD_ID));
        assertTrue(vm2.hasAttribute(Attribute.CREATED_AT));
        assertEquals("VMID", vm2.get(Attribute.VMID));
        assertTrue(vm2.hasAttribute(Attribute.PARENT_SEQUENCE_ID));
        assertTrue(vm2.hasAttribute(Attribute.SEQUENCE_ID));
        assertTrue(vm2.hasAttribute("inheritable"));

        vm2.fire();                 

        tm.done();

        Monitor[] monitors = _processor.extractProcessObjects();
        assertEquals(3, monitors.length);
        assertEquals(vm, monitors[0]);
        assertEquals(vm2, monitors[1]);
        assertEquals(tm, monitors[2]);
    }

    public void testCantOverwriteLockedAttributes() {
        ValueMonitor vm = new ValueMonitor("foo", 0.0d);
        vm.set(Attribute.NAME, "newName");
        vm.set(Attribute.VALUE, 10.0d);
        vm.set(Attribute.VMID, "newVmid");

        Object createdAt = vm.get(Attribute.CREATED_AT);
        Object threadId = vm.get(Attribute.THREAD_ID);
        vm.set(Attribute.CREATED_AT, new Date());
        vm.set(Attribute.THREAD_ID, "abc123");

        assertEquals("foo", vm.get(Attribute.NAME));
        assertEquals(0.0d, vm.get(Attribute.VALUE));
        assertEquals("VMID", vm.get(Attribute.VMID));
        assertEquals(createdAt, vm.get(Attribute.CREATED_AT));
        assertEquals(threadId, vm.get(Attribute.THREAD_ID));
    }
}
