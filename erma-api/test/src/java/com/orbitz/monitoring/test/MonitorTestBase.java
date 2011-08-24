package com.orbitz.monitoring.test;

import com.orbitz.monitoring.api.*;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract class that can be used to test implementations of {@link Monitor}
 * against the contract.
 *
 * @author Doug Barth
 */
public abstract class MonitorTestBase extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private MockMonitorProcessor _processor;
    private MockDecomposer _decomposer;

    // ** CONSTRUCTORS ********************************************************
    protected MonitorTestBase() {
    }

    protected MonitorTestBase(String string) {
        super(string);
    }

    // ** TEST SUITE METHODS **************************************************
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
    }

    protected void tearDown()
            throws Exception {
        MonitoringEngine.getInstance().shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testMonitorCreationInvariants() {
        Monitor monitor = createMonitor("test");

        assertEquals("monitor.name", "test", monitor.get(Attribute.NAME));
        assertTrue("monitor.createdAt",
                   monitor.hasAttribute(Attribute.CREATED_AT));
        assertEquals(Integer.toHexString(Thread.currentThread().hashCode()),
                     monitor.get(Attribute.THREAD_ID));
        assertNotNull(monitor.getLevel());
    }

    public void testAttributeNamingRequirements() {
        Monitor monitor = createMonitor("test");

        // Valid names follow Java variable naming rules
        monitor.set("foo", "bar");
        monitor.set("_foo", "bar");
        monitor.set("Foo", "bar");
        monitor.set("foo9", "bar");

        // Invalid names
        try {
            monitor.set("9foo", new Object());
            fail("RuntimeException expected");
        } catch (RuntimeException e) { /* Expected */ }
        try {
            monitor.set("foo.bar", new Object());
            fail("RuntimeException expected");
        } catch (RuntimeException e) { /* Expected */ }
    }

    public void testObjectAttributes() {
        Monitor monitor = createMonitor("test");

        Object key1Val = new Object();
        StringBuffer key2Val = new StringBuffer("foo");

        monitor.set("key1", key1Val);
        monitor.set("key2", key2Val);

        // Coerceable types
        assertSame("Value forT key1", key1Val, monitor.get("key1"));
        assertSame("Value for key2", key2Val, monitor.get("key2"));

        assertEquals("String value for key2", "foo",
                     monitor.getAsString("key2"));

        // Key undefined behavior
        assertUndefinedObject(monitor, "unknown");

        // Un-coerceable types
        assertCantCoerceToShort(monitor, "key1");
        assertCantCoerceToByte(monitor, "key1");
        assertCantCoerceToInt(monitor, "key1");
        assertCantCoerceToLong(monitor, "key1");
        assertCantCoerceToFloat(monitor, "key1");
        assertCantCoerceToDouble(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value
        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testStringAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", "value1");
        monitor.set("key2", "v");
        monitor.set("key3", "true");
        monitor.set("key4", "false");

        // Coerceable types
        assertEquals("Value for key1", "value1", monitor.getAsString("key1"));
        assertEquals("Value for key2", "v", monitor.getAsString("key2"));
        assertEquals("Value for key2 as char", 'v', monitor.getAsChar("key2"));
        assertEquals("Value for key3 as boolean", true,
                     monitor.getAsBoolean("key3"));
        assertEquals("Value for key4 as boolean", false,
                     monitor.getAsBoolean("key4"));

        assertEquals("Value as Object", "value1", monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedObject(monitor, "unknown");
        assertUndefinedString(monitor, "unknown");

        // Un-coerceable types
        // Only strings with length == 1 to can be coerced to a char.
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToShort(monitor, "key1");
        assertCantCoerceToByte(monitor, "key1");
        assertCantCoerceToInt(monitor, "key1");
        assertCantCoerceToLong(monitor, "key1");
        assertCantCoerceToFloat(monitor, "key1");
        assertCantCoerceToDouble(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value

        Object o = null;
        monitor.set("key2", o);

        assertNull("Value for key2", monitor.get("key2"));
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testShortAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", (short) 1);
        monitor.set("key2", (short) 2);
        monitor.set("key3", new Short((short) 3));

        // Coerceable types
        assertEquals("Value for key1", 1, monitor.getAsShort("key1"));
        assertEquals("Value for key2", 2, monitor.getAsShort("key2"));
        assertEquals("Value for key3", 3, monitor.getAsShort("key3"));

        assertEquals("Value for key1", 1, monitor.getAsShort("key1",(short) 2));

        assertEquals("Value as int", 1, monitor.getAsByte("key1"));
        assertEquals("Value as int", 1, monitor.getAsInt("key1"));
        assertEquals("Value as long", 1, monitor.getAsLong("key1"));
        assertEquals("Value as float", 1.0, monitor.getAsFloat("key1"), 0.0);
        assertEquals("Value as double", 1.0, monitor.getAsDouble("key1"), 0.0);
        assertEquals("Value as String", "1", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Short((short)1),
                     monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedShort(monitor, "unknown");
        assertEquals("Default value used", 5,
                     monitor.getAsShort("unknown", (short)5));

        // Un-coerceable types
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value

        Object o = null;
        monitor.set("key2", o);

        assertNull("Value for key2", monitor.get("key2"));
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testByteAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", (byte) 1);
        monitor.set("key2", (byte) 2);
        monitor.set("key3", new Byte((byte) 3));

        // Coerceable types
        assertEquals("Value for key1", 1, monitor.getAsByte("key1"));
        assertEquals("Value for key2", 2, monitor.getAsByte("key2"));
        assertEquals("Value for key3", 3, monitor.getAsByte("key3"));

        assertEquals("Value for key1", 1, monitor.getAsByte("key1", (byte) 2));

        assertEquals("Value as int", 1, monitor.getAsShort("key1"));
        assertEquals("Value as int", 1, monitor.getAsInt("key1"));
        assertEquals("Value as long", 1, monitor.getAsLong("key1"));
        assertEquals("Value as float", 1.0, monitor.getAsFloat("key1"), 0.0);
        assertEquals("Value as double", 1.0, monitor.getAsDouble("key1"), 0.0);
        assertEquals("Value as String", "1", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Byte((byte)1), monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedByte(monitor, "unknown");
        assertEquals("Default value used", 5,
                     monitor.getAsByte("unknown", (byte)5));

        // Un-coerceable types
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value

        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedByte(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testIntAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", 1);
        monitor.set("key2", 2);
        monitor.set("key3", new Integer(3));

        // Coerceable types
        assertEquals("Value for key1", 1, monitor.getAsInt("key1"));
        assertEquals("Value for key2", 2, monitor.getAsInt("key2"));
        assertEquals("Value for key3", 3, monitor.getAsInt("key3"));

        assertEquals("Value for key1", 1, monitor.getAsInt("key1", 2));

        assertEquals("Value as byte", 1, monitor.getAsShort("key1"));
        assertEquals("Value as byte", 1, monitor.getAsByte("key1"));
        assertEquals("Value as long", 1, monitor.getAsLong("key1"));
        assertEquals("Value as float", 1.0, monitor.getAsFloat("key1"), 0.0);
        assertEquals("Value as double", 1.0, monitor.getAsDouble("key1"), 0.0);
        assertEquals("Value as String", "1", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Integer(1), monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedInt(monitor, "unknown");
        assertEquals("Default value used", 5, monitor.getAsInt("unknown", 5));

        // Un-coerceable types
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value

        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedInt(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testLongAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", (long) 1);
        monitor.set("key2", (long) 2);
        monitor.set("key3", new Long((long) 3));

        // Coerceable types
        assertEquals("Value for key1", 1, monitor.getAsLong("key1"));
        assertEquals("Value for key2", 2, monitor.getAsLong("key2"));
        assertEquals("Value for key3", 3, monitor.getAsLong("key3"));

        assertEquals("Value for key1", 1, monitor.getAsLong("key1", 2));

        assertEquals("Value as byte", 1, monitor.getAsShort("key1"));
        assertEquals("Value as byte", 1, monitor.getAsByte("key1"));
        assertEquals("Value as int", 1, monitor.getAsInt("key1"));
        assertEquals("Value as float", 1.0, monitor.getAsFloat("key1"), 0.0);
        assertEquals("Value as double", 1.0, monitor.getAsDouble("key1"), 0.0);
        assertEquals("Value as String", "1", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Long(1), monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedLong(monitor, "unknown");
        assertEquals("Default value used", 5, monitor.getAsLong("unknown", 5));

        // Un-coerceable types
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value

        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedLong(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testFloatAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", (float) 1.5);
        monitor.set("key2", (float) 2.5);
        monitor.set("key3", new Float((float) 3.5));

        // Coerceable types
        assertEquals("Value for key1", (float) 1.5, monitor.getAsFloat("key1"),
                     0);
        assertEquals("Value for key2", (float) 2.5, monitor.getAsFloat("key2"),
                     0);
        assertEquals("Value for key3", (float) 3.5, monitor.getAsFloat("key3"),
                     0);

        assertEquals("Value for key1", (float) 1.5,
                     monitor.getAsFloat("key1", (float) 2.5), 0);

        assertEquals("Value as byte", 1, monitor.getAsShort("key1"));
        assertEquals("Value as byte", 1, monitor.getAsByte("key1"));
        assertEquals("Value as int", 1, monitor.getAsInt("key1"));
        assertEquals("Value as long", 1, monitor.getAsLong("key1"));
        assertEquals("Value as double", 1.5, monitor.getAsDouble("key1"), 0.0);
        assertEquals("Value as String", "1.5", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Float(1.5), monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedFloat(monitor, "unknown");
        assertEquals("Default value used", (float) 5.5,
                     monitor.getAsFloat("unknown", (float) 5.5), 0);

        // Un-coerceable types
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value
        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedLong(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testDoubleAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", 1.5);
        monitor.set("key2", 2.5);
        monitor.set("key3", 3.5);

        // Coerceable types
        assertEquals("Value for key1", 1.5, monitor.getAsDouble("key1"), 0);
        assertEquals("Value for key2", 2.5, monitor.getAsDouble("key2"), 0);
        assertEquals("Value for key3", 3.5, monitor.getAsDouble("key3"), 0);

        assertEquals("Value for key1", 1.5, monitor.getAsDouble("key1", 2.5),0);

        assertEquals("Value as byte", 1, monitor.getAsShort("key1"));
        assertEquals("Value as byte", 1, monitor.getAsByte("key1"));
        assertEquals("Value as int", 1, monitor.getAsInt("key1"));
        assertEquals("Value as long", 1, monitor.getAsLong("key1"));
        assertEquals("Value as float", 1.5, monitor.getAsFloat("key1"), 0.0);
        assertEquals("Value as String", "1.5", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Double(1.5), monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedDouble(monitor, "unknown");
        assertEquals("Default value used", 5.5,
                     monitor.getAsDouble("unknown", 5.5), 0);

        // Un-coerceable types
        assertCantCoerceToChar(monitor, "key1");
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value
        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedDouble(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testBooleanAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", true);
        monitor.set("key2", false);
        monitor.set("key3", Boolean.TRUE);
        monitor.set("key4", Boolean.FALSE);

        // Coerceable types
        assertEquals("Value for key1", true, monitor.getAsBoolean("key1"));
        assertEquals("Value for key2", false, monitor.getAsBoolean("key2"));
        assertEquals("Value for key3", true, monitor.getAsBoolean("key3"));
        assertEquals("Value for key4", false, monitor.getAsBoolean("key4"));

        assertEquals("Value for key1", true,
                     monitor.getAsBoolean("key1", false));

        assertEquals("Value as String", "true", monitor.getAsString("key1"));
        assertSame("Value as Object", Boolean.TRUE, monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedBoolean(monitor, "unknown");
        assertEquals("Default value used", true,
                     monitor.getAsBoolean("unknown", true));

        // Un-coerceable types
        assertCantCoerceToShort(monitor, "key1");
        assertCantCoerceToByte(monitor, "key1");
        assertCantCoerceToInt(monitor, "key1");
        assertCantCoerceToLong(monitor, "key1");
        assertCantCoerceToFloat(monitor, "key1");
        assertCantCoerceToDouble(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value
        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedBoolean(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testCharAttributes() {
        Monitor monitor = createMonitor("test");

        monitor.set("key1", 'a');
        monitor.set("key2", 'b');
        monitor.set("key3", new Character('c'));

        // Coerceable types
        assertEquals("Value for key1", 'a', monitor.getAsChar("key1"));
        assertEquals("Value for key2", 'b', monitor.getAsChar("key2"));
        assertEquals("Value for key3", 'c', monitor.getAsChar("key3"));

        assertEquals("Value for key1", 'a', monitor.getAsChar("key1", 'b'));

        assertEquals("Value as String", "a", monitor.getAsString("key1"));
        assertEquals("Value as Object", new Character('a'), monitor.get("key1"));

        // Key undefined behavior
        assertUndefinedChar(monitor, "unknown");
        assertEquals("Default value used", 'z',
                     monitor.getAsChar("unknown", 'z'));

        // Un-coerceable types
        assertCantCoerceToBoolean(monitor, "key1");
        assertCantCoerceToShort(monitor, "key1");
        assertCantCoerceToByte(monitor, "key1");
        assertCantCoerceToInt(monitor, "key1");
        assertCantCoerceToLong(monitor, "key1");
        assertCantCoerceToFloat(monitor, "key1");
        assertCantCoerceToDouble(monitor, "key1");
        assertCantCoerceToMap(monitor, "key1");
        assertCantCoerceToList(monitor, "key1");
        assertCantCoerceToSet(monitor, "key1");

        // Null value
        Object o = null;
        monitor.set("key2", o);
        assertNull("Value for key2", monitor.get("key2"));

        assertUndefinedChar(monitor, "key2");
        assertTrue("key2 defined", monitor.hasAttribute("key2"));
    }

    public void testMapAttributes() {
        Monitor monitor = createMonitor("test");

        Map map = new HashMap();
        map.put("foo", "bar");
        monitor.set("map", map);
        monitor.set("string", "foo");

        // Coerceable types
        assertSame(map, monitor.get("map"));
        assertEquals("bar", monitor.getAsMap("map").get("foo"));
        assertNotNull(monitor.getAsString("map"));

        // Key undefined behavior
        assertUndefinedMap(monitor, "undefined");

        // Un-coerceable types
        assertCantCoerceToBoolean(monitor, "map");
        assertCantCoerceToShort(monitor, "map");
        assertCantCoerceToByte(monitor, "map");
        assertCantCoerceToInt(monitor, "map");
        assertCantCoerceToLong(monitor, "map");
        assertCantCoerceToFloat(monitor, "map");
        assertCantCoerceToDouble(monitor, "map");
        assertCantCoerceToList(monitor, "map");
        assertCantCoerceToSet(monitor, "map");
    }

    public void testListAttributes() {
        Monitor monitor = createMonitor("test");

        List list = new ArrayList();
        list.add("a");

        List innerList = new ArrayList();
        innerList.add("b");
        list.add(innerList);

        list.add("c");
        monitor.set("list", list);

        // Coerceable types
        assertSame(list, monitor.get("list"));

        assertNotNull(monitor.getAsString("list"));

        // Key undefined behavior
        assertUndefinedList(monitor, "undefined");

        // Un-coerceable types
        assertCantCoerceToBoolean(monitor, "list");
        assertCantCoerceToShort(monitor, "list");
        assertCantCoerceToByte(monitor, "list");
        assertCantCoerceToInt(monitor, "list");
        assertCantCoerceToLong(monitor, "list");
        assertCantCoerceToFloat(monitor, "list");
        assertCantCoerceToDouble(monitor, "list");
        assertCantCoerceToMap(monitor, "list");
        assertCantCoerceToSet(monitor, "list");
    }

    public void testSetAttributes() {
        Monitor monitor = createMonitor("test");

        Set set = new HashSet();
        set.add("foo");
        monitor.set("set", set);

        // Coerceable types
        assertSame(set, monitor.get("set"));
        assertTrue(monitor.getAsSet("set").contains("foo"));
        assertNotNull(monitor.getAsString("set"));

        // Key undefined behavior
        assertUndefinedSet(monitor, "undefined");

        // Un-coerceable types
        assertCantCoerceToBoolean(monitor, "set");
        assertCantCoerceToShort(monitor, "set");
        assertCantCoerceToByte(monitor, "set");
        assertCantCoerceToInt(monitor, "set");
        assertCantCoerceToLong(monitor, "set");
        assertCantCoerceToFloat(monitor, "set");
        assertCantCoerceToDouble(monitor, "set");
        assertCantCoerceToMap(monitor, "set");
        assertCantCoerceToList(monitor, "set");
    }

    public void testObjectArrayAttributes() {
        Monitor monitor = createMonitor("test");

        Object[] array = new String[] {"foo", "bar"};
        monitor.set("array", array);

        // Coerceable types
        assertSame(array, monitor.get("array"));

        assertEquals("bar", monitor.getAsList("array").get(1));
        assertNotNull(monitor.getAsString("array"));

        // Key undefined behavior
        assertUndefinedList(monitor, "undefined");

        // Un-coerceable types
        assertCantCoerceToBoolean(monitor, "array");
        assertCantCoerceToShort(monitor, "array");
        assertCantCoerceToByte(monitor, "array");
        assertCantCoerceToInt(monitor, "array");
        assertCantCoerceToLong(monitor, "array");
        assertCantCoerceToFloat(monitor, "array");
        assertCantCoerceToDouble(monitor, "array");
        assertCantCoerceToMap(monitor, "array");
        assertCantCoerceToSet(monitor, "array");
    }

    public void testInheritanceHierarchy() {
        TransactionMonitor parent = new TransactionMonitor("parent");
        parent.setInheritable("implicit", "implicit");
        parent.setInheritable("explicit", "implicit");
        parent.setInheritable("engine", "implicit");

        Map explicitInheritance = new HashMap();
        explicitInheritance.put("explicit", "explicit");
        explicitInheritance.put("engine", "explicit");

        MonitoringEngine engine = MonitoringEngine.getInstance();
        engine.setGlobalAttribute("engine", "engine");

        // Grr, I need a way to test that instrumentation wins out over monitor
        // set attributes during construction.
        Monitor monitor = createMonitor("monitor", explicitInheritance);

        assertEquals("implicit", monitor.get("implicit"));
        assertEquals("explicit", monitor.get("explicit"));
        //assertEquals("engine", monitor.get("engine"));
    }

    public void testMonitorsDisabled() {
        MonitoringEngine.getInstance().setMonitoringEnabled(false);

        Monitor[] monitors = useMonitors();
        for (int i = 0; i < monitors.length; i++) {
            Monitor monitor = monitors[i];
            getMockProcessor(monitor).assertNoUnexpectedCalls();
        }
    }

    public void testSingleUseDefault() {
        Monitor monitor = createMonitor("test");

        completeMonitorUse(monitor);
        getMockProcessor(monitor).assertExpectedProcessObject(monitor);

        // Need to clear so we can check if we were called again
        getMockProcessor(monitor).clear();

        // By default, a second call to fire should not cause an exception to be
        // thrown. The metric should not be processed as well.
        completeMonitorUse(monitor);
        getMockProcessor(monitor).assertNoUnexpectedCalls();
    }

    public void testGetSerializableMomento() {
        Monitor monitor = createMonitor("test");

        // Primitives
        monitor.set("byte", (byte) 1);
        monitor.set("short", (short) 1);
        monitor.set("int", 1);
        monitor.set("long", (long) 1);
        monitor.set("float", (float) 1.0);
        monitor.set("double", 1.0);
        monitor.set("char", 'a');
        monitor.set("string", "string");
        monitor.set("boolean", true);

        // Collections
        Map map = new HashMap();
        map.put("foo", "bar");
        monitor.set("map", map);

        List list = new ArrayList();
        list.add("bar");
        monitor.set("list", list);

        Set set = new HashSet();
        set.add("foo");
        monitor.set("set", set);

        String[] stringArray = new String[]{"foo", "bar"};
        monitor.set("stringArray", stringArray);

        // Arbitrary objects
        monitor.set("foo", new Exception("bar"));

        SerializableMonitor momento = monitor.getSerializableMomento();
        assertSerializableEquivalent(monitor, momento);
    }

    public void testGetSerializableMomentoEngineDisabled() {
        Monitor monitor = createMonitor("test");

        // Primitives
        monitor.set("byte", (byte) 1);
        monitor.set("short", (short) 1);
        monitor.set("int", 1);
        monitor.set("long", (long) 1);
        monitor.set("float", (float) 1.0);
        monitor.set("double", 1.0);
        monitor.set("char", 'a');
        monitor.set("string", "string");
        monitor.set("boolean", true);

        MonitoringEngine.getInstance().setMonitoringEnabled(false);
        SerializableMonitor momento = monitor.getSerializableMomento();
        assertEquals(0, momento.getAll().size());
    }

    public void testUnset() {
        String key = "foo";
        Monitor monitor = createMonitor("test");

        monitor.set(key, "bar");
        monitor.unset(key);

        assertUndefinedString(monitor, key);
    }

    public void testSetAll() {
        Monitor monitor = createMonitor("firstMon");
        Monitor otherMonitor = createMonitor("secondMon");

        Map attributeHolders = new HashMap();
        attributeHolders.put("foo",monitor.set("foo", "bar").notSerializable());
        attributeHolders.put("baz",monitor.set("baz", "ccc"));

        otherMonitor.setAllAttributeHolders(attributeHolders);

        Map serializableAttributes = otherMonitor.getAllSerializable();
        
        assertEquals("Strings should be serializable by default","ccc", serializableAttributes.get("baz"));
    }

    // custom processor needed for next test...
    private static class CreateTestMonitorProcessor
            extends MockMonitorProcessor {
        public boolean failed = false;

        public void monitorCreated(Monitor m) {
            // next line won't work here, doug is too smart...
            //assertNotNull(m.get(Monitor.NAME));
            failed = (m.get(Attribute.NAME) == null);
            super.monitorCreated(m);
        }
    }

    public void testMonitorCreateCallback() {
        CreateTestMonitorProcessor processor = new CreateTestMonitorProcessor();
        MockMonitorProcessorFactory processorFactory =
                new MockMonitorProcessorFactory(
                        new MonitorProcessor[]{processor});

        MonitoringEngine mEngine = MonitoringEngine.getInstance();
        mEngine.setProcessorFactory(processorFactory);
        mEngine.restart();

        createMonitor("test");
        assertFalse(processor.failed);

        Monitor[] ms = processor.extractMonitorCreatedObjects();
        assertNotNull(ms);
        assertEquals(1, ms.length);
        assertNotNull(ms[0]);
        assertNotNull(ms[0].get(Attribute.NAME));
    }

    // ** PROTECTED METHODS ***************************************************
    protected MockMonitorProcessor getMockProcessor(Monitor monitor) {
        return _processor;
    }

    protected Monitor createMonitor(String name) {
        return createMonitor(name, null);
    }

    protected abstract Monitor createMonitor(
            String name, Map inheritedAttributes);

    protected abstract Monitor[] useMonitors();

    protected abstract void completeMonitorUse(Monitor monitor);


    protected void assertSerializableEquivalent(Monitor monitor,
                                                SerializableMonitor momento) {
        Map originalAttrs = monitor.getAllSerializable();
        Map serializableAttrs = momento.getAll();

        List decomposedObjects = _decomposer.getDecomposedObjects();
        
        assertEquals(originalAttrs.keySet(), serializableAttrs.keySet());
        for (Iterator i = originalAttrs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();

            Object momentoValue = serializableAttrs.get(key);

            assertTrue(momentoValue instanceof Serializable);
            assertTrue(decomposedObjects.contains(monitor.get(key)));
        }

        additionalChecks(monitor, momento);
    }

    protected void additionalChecks(Monitor monitor,
                                    SerializableMonitor momento) {
        // No-op
    }

    protected void assertUndefinedObject(Monitor monitor, String key) {
        try {
            monitor.get(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertUndefinedString(Monitor monitor, String key) {
        try {
            monitor.getAsString(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToMap(Monitor monitor, String key) {
        try {
            monitor.getAsMap(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }
    protected void assertUndefinedMap(Monitor monitor, String key) {
        try {
            monitor.getAsMap(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToList(Monitor monitor, String key) {
        try {
            monitor.getAsList(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }
    protected void assertUndefinedList(Monitor monitor, String key) {
        try {
            monitor.getAsList(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToSet(Monitor monitor, String key) {
        try {
            monitor.getAsSet(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }
    protected void assertUndefinedSet(Monitor monitor, String key) {
        try {
            monitor.getAsString(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }


    protected void assertCantCoerceToBoolean(Monitor monitor, String key) {
        try {
            monitor.getAsBoolean(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedBoolean(Monitor monitor, String key) {
        try {
            monitor.getAsBoolean(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToDouble(Monitor monitor, String key) {
        try {
            monitor.getAsDouble(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedDouble(Monitor monitor, String key) {
        try {
            monitor.getAsDouble(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToFloat(Monitor monitor, String key) {
        try {
            monitor.getAsFloat(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedFloat(Monitor monitor, String key) {
        try {
            monitor.getAsFloat(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToLong(Monitor monitor, String key) {
        try {
            monitor.getAsLong(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedLong(Monitor monitor, String key) {
        try {
            monitor.getAsLong(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToInt(Monitor monitor, String key) {
        try {
            monitor.getAsInt(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedInt(Monitor monitor, String key) {
        try {
            monitor.getAsInt(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToByte(Monitor monitor, String key) {
        try {
            monitor.getAsByte(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedByte(Monitor monitor, String key) {
        try {
            monitor.getAsByte(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }

    protected void assertCantCoerceToShort(Monitor monitor, String key) {
        try {
            monitor.getAsShort(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedShort(Monitor monitor, String key) {
        try {
            monitor.getAsShort(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }
    protected void assertCantCoerceToChar(Monitor monitor, String key) {
        try {
            monitor.getAsChar(key);
            fail("Exception should have been thrown");
        } catch (CantCoerceException e) {
            // Expected
        }
    }

    protected void assertUndefinedChar(Monitor monitor, String key) {
        try {
            monitor.getAsChar(key);
            fail("Exception should have been thrown");
        } catch (AttributeUndefinedException e) {
            // Expected
        }
    }
}

