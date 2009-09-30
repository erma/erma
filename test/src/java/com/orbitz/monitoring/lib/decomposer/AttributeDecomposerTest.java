package com.orbitz.monitoring.lib.decomposer;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.AttributeMap;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import junit.framework.TestCase;
import org.apache.commons.beanutils.LazyDynaBean;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Unit test for <code>AttributeDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class AttributeDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private AttributeDecomposer _decomposer;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _decomposer = new AttributeDecomposer();
    }

    // ** TEST METHODS ********************************************************
    public void testDecomposePrimitives() {

        List list = new ArrayList();
        list.add(new Node("foo"));
        CompositeAttributeHolder holder = new CompositeAttributeHolder(list);

        Serializable decomposed = _decomposer.decompose(holder);

        AttributeHolder dHolder = (AttributeHolder)decomposed;
        assertEquals("Should be a list of one", 1, ((List)dHolder.getValue()).size());


        AttributeHolder holder2 = new AttributeHolder("someString");

        Serializable decomposed2 = _decomposer.decompose(holder2);

        AttributeHolder dHolder2 = (AttributeHolder)decomposed2;
        assertEquals("Should be a string", "someString", (String)dHolder2.getValue());
    }

    public void testDecomposeMapsOfMaps() {
        Map topMap = new HashMap();
        Map innerMap = new HashMap();

        innerMap.put("foo", "bar");
        topMap.put("innerMap", innerMap);

        AttributeHolder holder = new AttributeHolder(topMap);
        Serializable decomposed = _decomposer.decompose(holder);

        EventMonitor monitor = new EventMonitor("test");
        monitor.set("topMap", decomposed);
        //assertEquals("bar", monitor.get("topMap.innerMap.foo"));
    }

    public void testDecomposeListOfList() {
        List topList = new ArrayList();
        List innerList = new ArrayList();
        innerList.add("bar");
        topList.add(innerList);

        Serializable decomposed = _decomposer.decompose(topList);

        EventMonitor monitor = new EventMonitor("test");
        monitor.set("topList", decomposed);
        // Can't do the following due to a limitation in commons-beanutils
        // monitor.get("topList[0][0]")
        // Must do this instead
        //assertEquals("bar", monitor.getAsList("topList[0]").get(0));
    }

    public void testDecomposeSet() {
        Set set = new HashSet();
        set.add("foo");

        Serializable decomposed = _decomposer.decompose(set);

        EventMonitor monitor = new EventMonitor("test");
        monitor.set("set", decomposed);
        //assertTrue(monitor.getAsSet("set").contains("foo"));
    }

    public void testDecomposeArray() {
        String[][] stringArray = new String[][]{
                new String[]{"foo"}, new String[]{"bar"}};

        Serializable decomposed = _decomposer.decompose(stringArray);

        EventMonitor monitor = new EventMonitor("test");
        monitor.set("stringArray", decomposed);
        //assertEquals("foo", monitor.getAsList("stringArray[0]").get(0));
        //assertEquals("bar", monitor.getAsList("stringArray[1]").get(0));
    }

    public static class OC {
        private Object _object;
        private OC _self;
        private Object _writeOnly;
        private InnerClass _foo;

        public OC(Object object) {
            _object = object;
            _self = this;
            _foo = new InnerClass();
        }

        public Object getObject() {
            return _object;
        }

        public OC getSelf() {
            return _self;
        }

        public void setWriteOnly(Object o) {
            _writeOnly = o;
        }

        public Object getFoo() {
            return _foo;
        }

        private static class InnerClass {
            private Object _innerFoo;

            public Object getInnerFoo() {
                return _innerFoo;
            }
        }
    }

    public void testDecomposeArbitraryObjects() {
        Exception foo = new Exception("bar");
        OC deepObject = new OC(new OC(new OC("foo")));

        EventMonitor monitor = new EventMonitor("test");
        monitor.set("foo", _decomposer.decompose(foo));
        monitor.set("deepObject", _decomposer.decompose(deepObject));
        monitor.set("klass", _decomposer.decompose(Object.class));

        //assertEquals("bar", monitor.get("foo.message"));
        //assertEquals("foo", monitor.get("deepObject.object.object.object"));
        //assertEquals("java.lang.Object", monitor.get("klass"));
    }

    public static class Node implements Serializable {
        private Set _next = new HashSet();
        private String _value;

        public Node(String value) {
            _value = value;
        }

        public String getValue() {
            return _value;
        }

        public Set getNext() {
            return _next;
        }

        public void addNext(Node next) {
            _next.add(next);
        }

        public void setNext(Set set) {
            _next = set;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Node node = (Node) o;

            // this was generated by IDEA:
            // if (_value != null ? !_value.equals(node._value) : node._value != null) return false;
            // return true;

            return (_value == null) ? (node._value == null) : _value.equals(node._value);
        }

        public int hashCode() {
            return (_value != null ? _value.hashCode() : 0);
        }
    }

    public void testDecomposeCircularGraph() throws Exception {
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");

        b.addNext(c);
        c.addNext(b);

        a.addNext(b);

        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.writeObject(a);

        EventMonitor m = new EventMonitor("test");
        m.set("a", _decomposer.decompose(a));
    }

    public void testEqualsVsIdentity() throws Exception {
        Node a1 = new Node("a");
        Node a2 = new Node("a");
        a1.addNext(a2);

        assertNotSame(a1, a2);

        ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        oos.writeObject(a1);

        EventMonitor m = new EventMonitor("test");
        m.set("a", _decomposer.decompose(a1));
    }

    public void testStringBufferSupport() {
        StringBuffer buf = new StringBuffer("abc");
        //assertEquals("abc", _decomposer.decompose(buf));
    }
}
