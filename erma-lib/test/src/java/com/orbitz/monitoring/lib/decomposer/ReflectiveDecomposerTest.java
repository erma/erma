package com.orbitz.monitoring.lib.decomposer;

import junit.framework.TestCase;
import org.apache.commons.beanutils.LazyDynaBean;

import java.util.IdentityHashMap;
import java.util.List;
import java.io.Serializable;

/**
 * Unit tests for <code>ReflectiveDecomposer</code>.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class ReflectiveDecomposerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private ReflectiveDecomposer _decomposer;
    private MockDecomposerStep _delegate;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _delegate = new MockDecomposerStep();
        _decomposer = new ReflectiveDecomposer(_delegate);
    }

    // ** TEST METHODS ********************************************************
    public void testDecomposeNull() {
        assertNull(_decomposer.decompose(null, new IdentityHashMap()));
    }

    public static class ExampleClass implements Serializable {
        private String _stringValue = "abc";
        private boolean _booleanValue = true;
        private Exception _exceptionValue = new Exception();

        public String getStringValue() {
            return _stringValue;
        }

        public boolean isBooleanValue() {
            return _booleanValue;
        }

        public Exception getExceptionValue() {
            return _exceptionValue;
        }
    }

    public void testDecompose() {
        ExampleClass original = new ExampleClass();

        LazyDynaBean decomposed = (LazyDynaBean) _decomposer.decompose(original, new IdentityHashMap());

        assertNotNull(decomposed.get("stringValue"));
        assertNotNull(decomposed.get("booleanValue"));
        assertNotNull(decomposed.get("exceptionValue"));

        List decomposedObjects = _delegate.getDecomposedObjects();
        assertTrue(decomposedObjects.contains(original.getStringValue()));
        assertTrue(decomposedObjects.contains(
                Boolean.valueOf(original.isBooleanValue())));
        assertTrue(decomposedObjects.contains(original.getExceptionValue()));
    }
}
