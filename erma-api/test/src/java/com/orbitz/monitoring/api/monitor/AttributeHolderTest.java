package com.orbitz.monitoring.api.monitor;

import junit.framework.TestCase;

/**
 * Tests {@link AttributeHolder}
 */
public class AttributeHolderTest extends TestCase {
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNullValues() {
    AttributeHolder holder1 = new AttributeHolder(null);
    AttributeHolder holder2 = new AttributeHolder(null);
    assertEquals(holder1, holder2);
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNullValuesOneLocked() {
    AttributeHolder holder1 = new AttributeHolder(null);
    AttributeHolder holder2 = new AttributeHolder(null).lock();
    assertFalse(holder1.equals(holder2));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNullValuesBothLocked() {
    AttributeHolder holder1 = new AttributeHolder(null).lock();
    AttributeHolder holder2 = new AttributeHolder(null).lock();
    assertEquals(holder1, holder2);
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNullValuesOneSerializable() {
    AttributeHolder holder1 = new AttributeHolder(null);
    AttributeHolder holder2 = new AttributeHolder(null).serializable();
    // Should pass because the serializable flag wasn't ever set because "null" is not serializable
    assertEquals(holder1, holder2);
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNullValuesBothSerializable() {
    AttributeHolder holder1 = new AttributeHolder(null).serializable();
    AttributeHolder holder2 = new AttributeHolder(null).serializable();
    assertEquals(holder1, holder2);
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNotNullValuesOneSerializable() {
    AttributeHolder holder1 = new AttributeHolder("a");
    AttributeHolder holder2 = new AttributeHolder("a").serializable();
    assertFalse(holder1.equals(holder2));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNotNullValuesBothSerializable() {
    AttributeHolder holder1 = new AttributeHolder("a").serializable();
    AttributeHolder holder2 = new AttributeHolder("a").serializable();
    assertEquals(holder1, holder2);
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsDifferentType() {
    AttributeHolder holder = new AttributeHolder(null);
    assertFalse(holder.equals("aString"));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNull() {
    AttributeHolder holder = new AttributeHolder(null);
    assertFalse(holder.equals(null));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNotNullNullValues() {
    AttributeHolder holder1 = new AttributeHolder("a");
    AttributeHolder holder2 = new AttributeHolder(null);
    assertFalse(holder1.equals(holder2));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsEqualValues() {
    AttributeHolder holder1 = new AttributeHolder("a");
    AttributeHolder holder2 = new AttributeHolder("a");
    assertTrue(holder1.equals(holder2));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNullNotNullValues() {
    AttributeHolder holder1 = new AttributeHolder(null);
    AttributeHolder holder2 = new AttributeHolder("a");
    assertFalse(holder1.equals(holder2));
  }
  
  /**
   * @see AttributeHolder#equals(Object)
   */
  public void testEqualsNotEqualValues() {
    AttributeHolder holder1 = new AttributeHolder("a");
    AttributeHolder holder2 = new AttributeHolder("b");
    assertFalse(holder1.equals(holder2));
  }
  
  /**
   * @see AttributeHolder#toString()
   */
  public void testToStringStringValue() {
    AttributeHolder holder = new AttributeHolder("a");
    assertEquals("a", holder.toString());
  }
  
  /**
   * @see AttributeHolder#toString()
   */
  public void testToStringNullValue() {
    AttributeHolder holder = new AttributeHolder(null);
    assertEquals("null", holder.toString());
  }
  
  /**
   * @see AttributeHolder#hashCode()
   */
  public void testHashCodeStringValue() {
    AttributeHolder holder1 = new AttributeHolder("a");
    AttributeHolder holder2 = new AttributeHolder("a");
    assertEquals(holder1.hashCode(), holder2.hashCode());
  }
  
  /**
   * @see AttributeHolder#hashCode()
   */
  public void testHashCodeNullValue() {
    AttributeHolder holder1 = new AttributeHolder(null);
    AttributeHolder holder2 = new AttributeHolder(null);
    assertEquals(holder1.hashCode(), holder2.hashCode());
  }
}
