package com.orbitz.monitoring.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.AttributeMap;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeMap;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * AttributeMapTest will unit test all functionality for the AttributeMap and AttributeHolder
 * classes. This includes testing the meta data about attributes (lock, serializable, inheritable)
 */
public class AttributeMapTest {
  private AttributeMap attributes;
  private CompositeAttributeMap compositeAttributes;
  
  @Before
  public void setUp() {
    compositeAttributes = new CompositeAttributeMap();
    attributes = new AttributeMap();
  }
  
  @After
  public void tearDown() {
    compositeAttributes = null;
    attributes = null;
  }
  
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
  
  @Test
  public void testNestedAttributes() {
    String nestedPropName = "foo.bar";
    
    Map nestedBean = new HashMap();
    
    // some condition to see if we should use propertyutilsbean
    if (nestedPropName.indexOf('.') != -1) {
      PropertyUtilsBean utilBean = new PropertyUtilsBean();
      try {
        utilBean.setProperty(nestedBean, "fo", "ba");
        utilBean.setNestedProperty(nestedBean, "1", 0);
        
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  @Test
  public void testNotSerializable() {
    NotSerializable value = new NotSerializable();
    AttributeHolder holder = new AttributeHolder(value).serializable();
    
    assertFalse("Object is not serializable", holder.isSerializable());
    
    Object nullValue = null;
    holder = new AttributeHolder(nullValue).serializable();
    
    assertFalse("Object is not serializable", holder.isSerializable());
  }
  
  /**
   * we assume the following will be serializable by default. this test ensures that setting these
   * values as serializable will actually mark them as serializable
   */
  @Test
  public void testSerializable() {
    
    AttributeHolder holder = new AttributeHolder(1).serializable();
    assertTrue("Primitive is serializable by default", holder.isSerializable());
    
    holder = new AttributeHolder(1.0).serializable();
    assertTrue("Primitive is serializable by default", holder.isSerializable());
    
    holder = new AttributeHolder(1l).serializable();
    assertTrue("Primitive is serializable by default", holder.isSerializable());
    
    holder = new AttributeHolder('x').serializable();
    assertTrue("Primitive is serializable by default", holder.isSerializable());
    
    holder = new AttributeHolder(false).serializable();
    assertTrue("Primitive is serializable by default", holder.isSerializable());
    
    holder = new AttributeHolder("foo").serializable();
    assertTrue("String is serializable by default", holder.isSerializable());
    
    holder = new AttributeHolder(new Date()).serializable();
    
    assertTrue("Date is serializable by default", holder.isSerializable());
  }
  
  @Test
  public void testNullAttributes() {
    attributes.setAllAttributeHolders(null);
    
    assertEquals(0, attributes.getAll().size());
  }
  
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
  
  @Test
  public void testSetAllAH() {
    Map map = new HashMap();
    AttributeHolder holder = new AttributeHolder("bar");
    map.put("foobar", holder);
    attributes.setAllAttributeHolders(map);
    
    assertEquals("Setting all attributes", attributes.get("foobar"), "bar");
    
    compositeAttributes.setAllAttributeHolders(map);
    assertEquals("Setting all attributes", compositeAttributes.get("foobar"), "bar");
    
  }
  
  @Test
  public void testSetAllCAH() {
    Map map = new HashMap();
    
    CompositeAttributeHolder cah = new CompositeAttributeHolder("baz");
    map.clear();
    map.put("foobar", cah);
    
    attributes.setAllAttributeHolders(map);
    assertEquals("Setting all attributes", attributes.get("foobar"), "baz");
    
    compositeAttributes.setAllAttributeHolders(map);
    assertEquals("Setting all attributes", compositeAttributes.get("foobar"), "baz");
  }
  
  @Test
  public void testSetAll() {
    Map map = new HashMap();
    map.put("key", "value");
    map.put("foo", "bar");
    
    attributes.setAll(map);
    assertEquals("Setting all attributes", attributes.get("foo"), "bar");
    assertEquals("Setting all attributes", attributes.getAll().size(), 2);
  }
  
  private class NotSerializable {
    public NotSerializable() {
      // no-op
    }
  }
}
