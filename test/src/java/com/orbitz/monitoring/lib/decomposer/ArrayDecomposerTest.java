package com.orbitz.monitoring.lib.decomposer;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Unit tests for <code>ArrayDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class ArrayDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private ArrayDecomposer _decomposer;
    private MockDecomposerStep _delegate;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _delegate = new MockDecomposerStep();
        _decomposer = new ArrayDecomposer(_delegate);
    }

    // ** TEST METHODS ********************************************************
    public void testDecomposeNull() {
        assertNull(_decomposer.decompose(null, new IdentityHashMap()));
    }

    public void testDecompose() {
        Object[] original = new Object[2];
        original[0] = "abc";
        original[1] = new Exception();

        Object[] decomposed = (Object[]) _decomposer.decompose(
                original, new IdentityHashMap());
        assertEquals(original.length, decomposed.length);

        List decomposedObjects = _delegate.getDecomposedObjects();
        assertTrue(decomposedObjects.containsAll(Arrays.asList(original)));
    }
}
