package com.orbitz.monitoring.lib.decomposer;

import junit.framework.TestCase;

import java.util.IdentityHashMap;

/**
 * Unit tests for <code>IdentityDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class IdentityDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private IdentityDecomposer _decomposer;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _decomposer = new IdentityDecomposer();
    }

    // ** TEST METHODS ********************************************************
    public void testDecompose() {
        String string = "abc";
        Integer anInteger = new Integer(3);

        assertSame(string, _decomposer.decompose(string, new IdentityHashMap()));
        assertSame(anInteger, _decomposer.decompose(anInteger, new IdentityHashMap()));
        assertNull(_decomposer.decompose(null, new IdentityHashMap()));

        try {
            _decomposer.decompose(new Object(), new IdentityHashMap());
            fail("ClassCastException should have been thrown");
        } catch (ClassCastException e) {
            // Expected
        }
    }
}
