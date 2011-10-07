package com.orbitz.monitoring.test;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import java.util.Iterator;
import java.util.Map;

/**
 * Used to test implementations of {@link CompositeMonitor}
 * @author Doug Barth
 */
public abstract class CompositeMonitorTestBase extends MonitorTestBase {
  private static final String INHERITABLE = "inheritable";
  private static final String NOT_INHERITABLE = "notInheritable";
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableObjectAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, new Object());
    monitor.set(NOT_INHERITABLE, new Object());
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableShortAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, (short)3);
    monitor.set(NOT_INHERITABLE, (short)4);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableByteAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, (byte)3);
    monitor.set(NOT_INHERITABLE, (byte)4);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableIntAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, 3);
    monitor.set(NOT_INHERITABLE, 4);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableLongAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, (long)3);
    monitor.set(NOT_INHERITABLE, (long)4);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableFloatAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, (float)3.0);
    monitor.set(NOT_INHERITABLE, (float)4.0);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableDoubleAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, 3.0);
    monitor.set(NOT_INHERITABLE, 4.0);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableBooleanAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, false);
    monitor.set(NOT_INHERITABLE, true);
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getInheritableAttributes()
   */
  public void testInheritableCharAttributes() {
    CompositeMonitor monitor = createCompositeMonitor("test");
    
    monitor.setInheritable(INHERITABLE, 'c');
    monitor.set(NOT_INHERITABLE, 'd');
    
    assertInheritableBehavior(monitor);
  }
  
  /**
   * @see CompositeMonitor#getSerializableMomento()
   * @see com.orbitz.monitoring.test.MonitorTestBase#testGetSerializableMomento()
   */
  @Override
  public void testGetSerializableMomento() {
    CompositeMonitor parent = createCompositeMonitor("parent");
    parent.set("parent", "foo");
    CompositeMonitor child = createCompositeMonitor("child");
    parent.set("child", "foo");
    CompositeMonitor grandchild = createCompositeMonitor("grandchild");
    parent.set("grandchild", "foo");
    
    completeMonitorUse(grandchild);
    completeMonitorUse(child);
    completeMonitorUse(parent);
    
    assertEquals(1, parent.getChildMonitors().size());
    assertEquals(1, child.getChildMonitors().size());
    assertEquals(0, grandchild.getChildMonitors().size());
    
    SerializableCompositeMonitor momento = (SerializableCompositeMonitor)parent
        .getSerializableMomento();
    
    assertSerializableEquivalent(parent, momento);
  }
  
  /**
   * Creates a {@link CompositeMonitor} that will be used in tests
   * @param name the monitor name
   * @return a new monitor
   */
  protected CompositeMonitor createCompositeMonitor(final String name) {
    return (CompositeMonitor)createMonitor(name);
  }
  
  @Override
  protected void additionalChecks(final Monitor monitor, final SerializableMonitor momento) {
    super.additionalChecks(monitor, momento);
    
    if (monitor instanceof CompositeMonitor) {
      CompositeMonitor tMon = (TransactionMonitor)monitor;
      SerializableCompositeMonitor cMomento = (SerializableCompositeMonitor)momento;
      
      assertEquals(tMon.getChildMonitors().size(), cMomento.getChildMonitors().size());
      
      Iterator<Monitor> i = tMon.getChildMonitors().iterator();
      Iterator<SerializableMonitor> j = cMomento.getSerializableChildMonitors().iterator();
      
      while (i.hasNext()) {
        Monitor childMon = i.next();
        SerializableMonitor childMomento = j.next();
        
        assertSerializableEquivalent(childMon, childMomento);
      }
    }
  }
  
  private void assertInheritableBehavior(final CompositeMonitor monitor) {
    assertTrue(monitor.hasAttribute(INHERITABLE));
    assertTrue(monitor.hasAttribute(NOT_INHERITABLE));
    
    Map<String, Object> inheritableAttributes = monitor.getInheritableAttributes();
    
    assertEquals(monitor.get(INHERITABLE), inheritableAttributes.get(INHERITABLE));
  }
}
