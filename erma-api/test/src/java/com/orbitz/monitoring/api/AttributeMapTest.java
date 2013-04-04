package com.orbitz.monitoring.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.AttributeMap;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeMap;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link AttributeMap}, {@link AttributeHolder}, {@link CompositeAttributeMap} and
 * {@link CompositeAttributeHolder}. This includes testing the meta data about attributes (lock,
 * serializable, inheritable)<br>
 * TODO: This should only test {@link AttributeMap}
 */
public class AttributeMapTest {
  private AttributeMap attributes;
  private CompositeAttributeMap compositeAttributes;
  
  /**
   * Prepares for each test
   */
  @Before
  public void setUp() {
    compositeAttributes = new CompositeAttributeMap();
    attributes = new AttributeMap();
  }
  
  /**
   * Cleans up after each test
   */
  @After
  public void tearDown() {
    compositeAttributes = null;
    attributes = null;
  }
  
  /**
   * @see AttributeMap#get(String)
   * @see AttributeMap#set(String, Object)
   * @see AttributeHolder#serializable()
   */
  @Test
  public void testAdd() {
    attributes.set("foo", "bar");
    
    String bar = (String)attributes.get("foo");
    assertEquals("object should not be modified when set", "bar", bar);
    
    AttributeHolder holder = attributes.set("foo", "baz").serializable();
    assertEquals("Value should be updated", "baz", attributes.get("foo"));
    assertTrue("Serializable should be true", holder.isSerializable());
  }
  
  /**
   * @see AttributeMap#getAsList(String)
   */
  @Test
  public void testGetAsList() {
    String key = "key";
    List<String> value = new ArrayList<String>();
    AttributeMap map = spy(new AttributeMap());
    doReturn(value).when(map).get(key);
    assertSame(value, map.getAsList(key));
  }
  
  /**
   * @see AttributeMap#getAsList(String)
   */
  @Test
  public void testGetAsListArray() {
    String key = "key";
    String[] value = new String[] {"a", "b", "c"};
    AttributeMap map = spy(new AttributeMap());
    doReturn(value).when(map).get(key);
    assertEquals(Lists.newArrayList(value), map.getAsList(key));
  }
  
  /**
   * @see AttributeMap#getAsList(String)
   */
  @Test
  public void testGetAsList2DArray() {
    String key = "key";
    String[][] value = new String[][] {new String[] {"a"}, new String[] {"b"}, new String[] {"c"}};
    AttributeMap map = spy(new AttributeMap());
    doReturn(value).when(map).get(key);
    final List<List<String>> actual = map.getAsList(key);
    assertEquals(actual.get(0).get(0), "a");
    assertEquals(actual.get(1).get(0), "b");
    assertEquals(actual.get(2).get(0), "c");
  }
  
  /**
   * @see AttributeMap#getAsList(String)
   */
  @Test
  public void testGetAsList3DArray() {
    String key = "key";
    String[][][] value = new String[][][] {new String[][] {new String[] {"a"}}};
    AttributeMap map = spy(new AttributeMap());
    doReturn(value).when(map).get(key);
    final List<List<List<String>>> actual = map.getAsList(key);
    assertEquals(actual.get(0).get(0).get(0), "a");
  }
  
  /**
   * @see AttributeMap#getAsSet(String)
   */
  @Test
  public void testGetAsSet() {
    String key = "key";
    HashSet<String> value = new HashSet<String>();
    AttributeMap map = spy(new AttributeMap());
    doReturn(value).when(map).get(key);
    assertEquals(value, map.getAsSet(key));
  }
  
  /**
   * @see AttributeMap#getAsSet(String)
   */
  @Test(expected = AttributeUndefinedException.class)
  public void testGetAsSetNotExists() {
    String key = "key";
    AttributeUndefinedException exception = new AttributeUndefinedException("attribute");
    AttributeMap map = spy(new AttributeMap());
    doThrow(exception).when(map).get(key);
    map.getAsSet(key);
  }
  
  /**
   * @see AttributeMap#getAsSet(String)
   */
  @Test(expected = CantCoerceException.class)
  public void testGetAsSetNotASet() {
    String key = "key";
    AttributeMap map = spy(new AttributeMap());
    doReturn("value").when(map).get(key);
    map.getAsSet(key);
  }
  
  /**
   * @see AttributeMap#set(String, Object)
   * @see AttributeHolder#serializable()
   * @see AttributeHolder#lock()
   * @see AttributeHolder#notSerializable()
   */
  @Test
  public void testMetaData() {
    AttributeHolder holder = attributes.set("foo", "bar");
    assertFalse("Default behavior is attributes are not serializable", holder.isSerializable());
    assertFalse("Default behavior is attributes are not locked", holder.isLocked());
    
    holder.serializable().lock();
    assertTrue("Attribute is now serializable", holder.isSerializable());
    assertTrue("Attribute is now locked", holder.isLocked());
    
    attributes.set("foo", "baz");
    assertEquals("Value should not be updated b/c attribute is locked", "bar",
        attributes.get("foo"));
    
    attributes.set("foo", "baz");
    assertEquals("Value should not be updated b/c attribute is locked", "bar", holder.getValue());
    assertEquals("Value should not be updated b/c attribute is locked", "bar",
        attributes.get("foo"));
    
    AttributeHolder h = attributes.set("one", "two").notSerializable();
    assertFalse(h.isSerializable());
    
    h = attributes.set("one", "three");
    assertFalse(h.isSerializable());
    
  }
  
  /**
   * @see CompositeAttributeHolder#setInheritable(boolean)
   * @see CompositeAttributeMap#set(String, Object)
   * @see TransactionMonitor
   */
  @Test
  public void testCompositeAttribute() {
    CompositeAttributeHolder holder = (CompositeAttributeHolder)compositeAttributes.set("foo",
        "bar");
    
    holder.setInheritable(true);
    
    assertTrue("Attribute should be inheritable", holder.isInheritable());
    
    TransactionMonitor foo = new TransactionMonitor("foo");
    
    holder = foo.setInheritable("x", "y");
    assertTrue("Attribute has been set to inheritable", holder.isInheritable());
    
    holder = (CompositeAttributeHolder)foo.set("x", "z");
    assertTrue("Updating value does not clear meta data", holder.isInheritable());
  }
  
  /**
   * @see AttributeMap#set(String, Object)
   */
  @Test
  public void testAddBadAttribute() {
    try {
      attributes.set("x.y|z", "foo");
      fail("An exception should be thrown when an attribute's key in invalid");
    }
    catch (IllegalArgumentException iae) {
      // expected
    }
  }
  
  /**
   * @see AttributeHolder#serializable()
   */
  @Test
  public void testNotSerializable() {
    NotSerializable value = new NotSerializable();
    AttributeHolder holder = new AttributeHolder(value).serializable();
    
    assertFalse("Object is not serializable", holder.isSerializable());
    
    holder = new AttributeHolder(null).serializable();
    
    assertFalse("Object is not serializable", holder.isSerializable());
  }
  
  /**
   * @see AttributeHolder#serializable()
   */
  @Test
  public void testSerializable() {
    AttributeHolder holder = new AttributeHolder(1).serializable();
    assertTrue(holder.isSerializable());
  }
  
  /**
   * @see AttributeMap#setAllAttributeHolders(Map)
   * @see AttributeMap#getAll()
   */
  @Test
  public void testNullAttributes() {
    attributes.setAllAttributeHolders(null);
    
    assertEquals(0, attributes.getAll().size());
  }
  
  /**
   * @see AttributeMap#set(String, Object)
   * @see AttributeMap#getAsShort(String)
   * @see AttributeMap#getAsFloat(String)
   * @see AttributeMap#getAsString(String)
   */
  @Test
  public void testGets() {
    attributes.set("dne", null);
    try {
      
      attributes.getAsShort("dne");
      fail("exception should have been thrown");
    }
    catch (AttributeUndefinedException e) {
      // expected
    }
    
    try {
      
      attributes.getAsFloat("dne");
      fail("exception should have been thrown");
    }
    catch (AttributeUndefinedException e) {
      // expected
    }
    
    assertNull(attributes.getAsString("dne"));
  }
  
  /**
   * @see AttributeMap#setAllAttributeHolders(Map)
   * @see AttributeMap#get(String)
   * @see CompositeAttributeMap#setAllAttributeHolders(Map)
   * @see CompositeAttributeMap#get(String)
   */
  @Test
  public void testSetAllAH() {
    Map<String, AttributeHolder> map = new HashMap<String, AttributeHolder>();
    AttributeHolder holder = new AttributeHolder("bar");
    map.put("foobar", holder);
    attributes.setAllAttributeHolders(map);
    
    assertEquals("Setting all attributes", attributes.get("foobar"), "bar");
    
    compositeAttributes.setAllAttributeHolders(map);
    assertEquals("Setting all attributes", compositeAttributes.get("foobar"), "bar");
    
  }
  
  /**
   * @see AttributeMap#setAllAttributeHolders(Map)
   * @see AttributeMap#get(String)
   * @see CompositeAttributeMap#setAllAttributeHolders(Map)
   * @see CompositeAttributeMap#get(String)
   */
  @Test
  public void testSetAllCAH() {
    Map<String, CompositeAttributeHolder> map = new HashMap<String, CompositeAttributeHolder>();
    
    CompositeAttributeHolder cah = new CompositeAttributeHolder("baz");
    map.clear();
    map.put("foobar", cah);
    
    attributes.setAllAttributeHolders(map);
    assertEquals("Setting all attributes", attributes.get("foobar"), "baz");
    
    compositeAttributes.setAllAttributeHolders(map);
    assertEquals("Setting all attributes", compositeAttributes.get("foobar"), "baz");
  }
  
  /**
   * @see AttributeMap#setAll(Map)
   * @see AttributeMap#get(String)
   * @see AttributeMap#getAll()
   */
  @Test
  public void testSetAll() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("key", "value");
    map.put("foo", "bar");
    
    attributes.setAll(map);
    assertEquals("Setting all attributes", attributes.get("foo"), "bar");
    assertEquals("Setting all attributes", attributes.getAll().size(), 2);
  }
  
  private static class NotSerializable {
  }
}
