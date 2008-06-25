package com.orbitz.monitoring.lib.decomposer;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Unit tests for <code>ListDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class ListDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private MockDecomposerStep _delegateDecomposer;
    private ListDecomposer _decomposer;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _delegateDecomposer = new MockDecomposerStep();
        _decomposer = new ListDecomposer(_delegateDecomposer);
    }

    // ** TEST METHODS ********************************************************
    public void testDecomposeList() {
        List originalList = new ArrayList();
        originalList.add("abc");
        originalList.add(new Integer(1));
        originalList.add(null);
        originalList.add(new Object());

        List decomposedList = (List) _decomposer.decompose(
                originalList, new IdentityHashMap());
        assertEquals(originalList.size(), decomposedList.size());
        for (int i = 0; i < decomposedList.size(); i++) {
            if (originalList.get(i) == null) {
                assertNull(decomposedList.get(i));
            } else {
                assertNotNull(decomposedList.get(i));
            }
        }

        List decomposedObjects = _delegateDecomposer.getDecomposedObjects();
        assertTrue(decomposedObjects.containsAll(originalList));
    }

    public void testDecomposeNull() {
        assertNull(_decomposer.decompose(null, new IdentityHashMap()));
    }
}
