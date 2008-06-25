package com.orbitz.monitoring.test;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

/**
 * An abstract test case that can be used to test implementations of
 * <code>CompositeMonitor</code> against the contract.
 *
 * @author Doug Barth
 */
public abstract class CompositeMonitorTestBase extends MonitorTestBase {
    // ** STATIC/FINAL DATA ***************************************************
    private static final String INHERITABLE = "inheritable";
    private static final String NOT_INHERITABLE = "notInheritable";

    // ** TEST METHODS ********************************************************
    public void testInheritableObjectAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, new Object());
        monitor.set(NOT_INHERITABLE, new Object());

        assertInheritableBehavior(monitor);
    }

    public void testInheritableShortAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, (short) 3);
        monitor.set(NOT_INHERITABLE, (short) 4);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableByteAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, (byte) 3);
        monitor.set(NOT_INHERITABLE, (byte) 4);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableIntAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, 3);
        monitor.set(NOT_INHERITABLE, 4);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableLongAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, (long) 3);
        monitor.set(NOT_INHERITABLE, (long) 4);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableFloatAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, (float) 3.0);
        monitor.set(NOT_INHERITABLE, (float) 4.0);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableDoubleAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, 3.0);
        monitor.set(NOT_INHERITABLE, 4.0);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableBooleanAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, false);
        monitor.set(NOT_INHERITABLE, true);

        assertInheritableBehavior(monitor);
    }

    public void testInheritableCharAttributes() {
        CompositeMonitor monitor = createCompositeMonitor("test");

        monitor.setInheritable(INHERITABLE, 'c');
        monitor.set(NOT_INHERITABLE, 'd');

        assertInheritableBehavior(monitor);
    }

    public void testGetSerializableMomento() {
        super.testGetSerializableMomento();
        
        CompositeMonitor parent = createCompositeMonitor("parent");
        parent.set("parent", "foo");
        CompositeMonitor child = createCompositeMonitor("child");
        parent.set("child", "foo");
        CompositeMonitor grandchild = createCompositeMonitor("grandchild");
        parent.set("grandchild", "foo");
        completeMonitorUse(grandchild);
        completeMonitorUse(child);
        completeMonitorUse(parent);

        SerializableCompositeMonitor momento = (SerializableCompositeMonitor)
                parent.getSerializableMomento();

        assertSerializableEquivalent(parent, momento);
    }

    // ** PROTECTED METHODS ***************************************************
    protected CompositeMonitor createCompositeMonitor(String name) {
        return (CompositeMonitor) createMonitor(name);
    }

    protected void additionalChecks(
            Monitor monitor, SerializableMonitor momento) {
        super.additionalChecks(monitor, momento);

        if (monitor instanceof CompositeMonitor) {
            CompositeMonitor tMon = (TransactionMonitor) monitor;
            SerializableCompositeMonitor cMomento =
                    (SerializableCompositeMonitor) momento;

            assertEquals(tMon.getChildMonitors().size(),
                         cMomento.getChildMonitors().size());

            Iterator i = tMon.getChildMonitors().iterator();
            Iterator j = cMomento.getChildMonitors().iterator();

            while (i.hasNext()) {
                Monitor childMon = (Monitor) i.next();
                SerializableMonitor childMomento = (SerializableMonitor)
                        j.next();

                assertSerializableEquivalent(childMon, childMomento);
            }
        }
    }

    private void assertInheritableBehavior(CompositeMonitor monitor) {
        assertTrue(monitor.hasAttribute(INHERITABLE));
        assertTrue(monitor.hasAttribute(NOT_INHERITABLE));

        Map inheritableAttributes = monitor.getInheritableAttributes();
        
        assertEquals(monitor.get(INHERITABLE), inheritableAttributes.get(INHERITABLE));
    }
}
