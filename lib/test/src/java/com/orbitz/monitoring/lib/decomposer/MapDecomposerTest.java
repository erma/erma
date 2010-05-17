package com.orbitz.monitoring.lib.decomposer;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

/**
 * Unit test for <code>MapDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class MapDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private MapDecomposer _decomposer;
    private MockDecomposerStep _delegate;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _delegate = new MockDecomposerStep();
        _decomposer = new MapDecomposer(_delegate);
    }

    // ** TEST METHODS ********************************************************
    public void testNullObject() {
        assertNull(_decomposer.decompose(null, new IdentityHashMap()));
    }

    public void testDecompose() {
        Map original = new HashMap();
        original.put("foo", "bar");
        original.put("baz", null);
        original.put("exception", new Exception());

        Map decomposed = (Map) _decomposer.decompose(original, new IdentityHashMap());
        assertEquals(original.size(), decomposed.size());

        List decomposedObjects = _delegate.getDecomposedObjects();
        for (Iterator i = original.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Object key = entry.getKey();
            Object value = entry.getValue();

            assertTrue(decomposedObjects.contains(key));
            assertTrue(decomposedObjects.contains(value));
            assertTrue(decomposed.containsKey(key));
            if (value == null) {
                assertNull(decomposed.get(key));
            } else {
                assertNotNull(decomposed.get(key));
            }
        }
    }
}
