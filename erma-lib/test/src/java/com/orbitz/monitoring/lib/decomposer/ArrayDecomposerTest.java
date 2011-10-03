package com.orbitz.monitoring.lib.decomposer;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests ArrayDecomposer
 * @author Doug Barth
 */
public class ArrayDecomposerTest {
  private ArrayDecomposer _decomposer;
  private MockDecomposerStep _delegate;
  
  /**
   * Prepares for each test
   */
  @Before
  public void setUp() {
    _delegate = new MockDecomposerStep();
    _decomposer = new ArrayDecomposer(_delegate);
  }
  
  /**
   * see ArrayDecomposer#decompose(Object, IdentityHashMap)
   */
  @Test
  public void testDecomposeNull() {
    assertNull(_decomposer.decompose(null, new IdentityHashMap<Object, Serializable>()));
  }
  
  /**
   * see ArrayDecomposer#decompose(Object, IdentityHashMap)
   */
  public void testDecompose() {
    Object[] original = new Object[2];
    original[0] = "abc";
    original[1] = new Exception();
    Object[] decomposed = (Object[])_decomposer.decompose(original,
        new IdentityHashMap<Object, Serializable>());
    assertEquals(original.length, decomposed.length);
    List<Object> decomposedObjects = _delegate.getDecomposedObjects();
    assertTrue(decomposedObjects.containsAll(Arrays.asList(original)));
  }
}
