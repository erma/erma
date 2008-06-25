package com.orbitz.monitoring.lib.decomposer;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Unit test for <code>SetDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */

public class SetDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private SetDecomposer _decomposer;
    private MockDecomposerStep _delegate;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _delegate = new MockDecomposerStep();
        _decomposer = new SetDecomposer(_delegate);
    }

    // ** TEST METHODS ********************************************************
    public void testDecomposeNull() {
        assertNull(_decomposer.decompose(null, new IdentityHashMap()));
    }

    public void testDecompose() {
        Set original = new HashSet();
        original.add("foo");
        original.add(new Exception());

        Set decomposed = (Set) _decomposer.decompose(original, new IdentityHashMap());
        assertEquals(original.size(), decomposed.size());

        List decomposedObjects = _delegate.getDecomposedObjects();
        assertTrue(decomposedObjects.containsAll(original));
    }
}
