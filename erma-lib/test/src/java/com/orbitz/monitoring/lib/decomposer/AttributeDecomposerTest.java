package com.orbitz.monitoring.lib.decomposer;

import static org.junit.Assert.*;

import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.MethodUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link AttributeDecomposer}
 * @author Doug Barth
 */
public class AttributeDecomposerTest {
  private AttributeDecomposer _decomposer;
  
  /**
   * Prepares for each test
   */
  @Before
  public void setUp() {
    _decomposer = new AttributeDecomposer();
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeOC() {
    OC<OC<OC<String>>> deepObject = new OC<OC<OC<String>>>(
        new OC<OC<String>>(new OC<String>("foo")));
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("deepObject", _decomposer.decompose(deepObject));
    final DynaBean actualDeepObject = (DynaBean)monitor.get("deepObject");
    final DynaBean subActual = (DynaBean)actualDeepObject.get("object");
    final DynaBean subSubActual = (DynaBean)subActual.get("object");
    String value = ((String)subSubActual.get("object"));
    assertEquals("foo", value);
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeClass() {
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("klass", _decomposer.decompose(Object.class));
    assertEquals("java.lang.Object", monitor.get("klass"));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeException() {
    Exception foo = new Exception("bar");
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("foo", _decomposer.decompose(foo));
    final DynaBean actualException = (DynaBean)monitor.get("foo");
    assertEquals("bar", actualException.get("message"));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeArray() {
    String[][] stringArray = new String[][] {new String[] {"foo"}, new String[] {"bar"}};
    Serializable decomposed = _decomposer.decompose(stringArray);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("stringArray", decomposed);
    final List<List<String>> actual = monitor.getAsList("stringArray");
    assertEquals("foo", actual.get(0).get(0));
    assertEquals("bar", actual.get(1).get(0));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   * @throws Exception in case of failure
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testDecomposeCircularGraph() throws Exception {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    b.addNext(c);
    c.addNext(b);
    a.addNext(b);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("a", _decomposer.decompose(a));
    final DynaBean actualA = (DynaBean)monitor.get("a");
    final DynaBean actualB = ((Set<DynaBean>)actualA.get("next")).iterator().next();
    final DynaBean actualC = ((Set<DynaBean>)actualB.get("next")).iterator().next();
    assertEquals("a", actualA.get("value"));
    assertEquals("b", actualB.get("value"));
    assertEquals("c", actualC.get("value"));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeListOfList() {
    List<List<String>> topList = new ArrayList<List<String>>();
    List<String> innerList = new ArrayList<String>();
    innerList.add("bar");
    topList.add(innerList);
    Serializable decomposed = _decomposer.decompose(topList);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("topList", decomposed);
    final List<List<String>> actual = monitor.getAsList("topList");
    assertEquals("bar", actual.get(0).get(0));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeMapsOfMaps() {
    Map<String, Map<String, String>> topMap = new HashMap<String, Map<String, String>>();
    Map<String, String> innerMap = new HashMap<String, String>();
    innerMap.put("foo", "bar");
    topMap.put("innerMap", innerMap);
    AttributeHolder holder = new AttributeHolder(topMap);
    Serializable decomposed = _decomposer.decompose(holder);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("holder", decomposed);
    final AttributeHolder actual = (AttributeHolder)monitor.get("holder");
    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> actualTopMap = (Map<String, Map<String, String>>)actual
        .getValue();
    assertEquals(topMap, actualTopMap);
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testDecomposePrimitives() {
    List<Node> list = new ArrayList<Node>();
    list.add(new Node("foo"));
    CompositeAttributeHolder holder = new CompositeAttributeHolder(list);
    AttributeHolder decomposedHolder = (AttributeHolder)_decomposer.decompose(holder);
    assertEquals(list.get(0).value,
        ((List<DynaBean>)decomposedHolder.getValue()).get(0).get("value"));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposePrimitives2() {
    AttributeHolder holder = new AttributeHolder("someString");
    AttributeHolder decomposedHolder = (AttributeHolder)_decomposer.decompose(holder);
    assertEquals(holder.getValue(), decomposedHolder.getValue());
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testDecomposeSet() {
    Set set = new HashSet();
    set.add("foo");
    
    Serializable decomposed = _decomposer.decompose(set);
    
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("set", decomposed);
    // assertTrue(monitor.getAsSet("set").contains("foo"));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testEqualsVsIdentity() throws Exception {
    Node a1 = new Node("a");
    Node a2 = new Node("a");
    a1.addNext(a2);
    
    assertNotSame(a1, a2);
    
    ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
    oos.writeObject(a1);
    
    EventMonitor m = new EventMonitor("test");
    m.set("a", _decomposer.decompose(a1));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testStringBufferSupport() {
    new StringBuffer("abc");
  }
  
  /**
   * This class is public so that {@link MethodUtils}, which is used by {@link ReflectiveDecomposer}
   * , can see it
   */
  public static class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    private Set<Node> next = new HashSet<Node>();
    private final String value;
    
    public Node(final String value) {
      this.value = value;
    }
    
    public void addNext(final Node next) {
      this.next.add(next);
    }
    
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      
      final Node node = (Node)o;
      
      // this was generated by IDEA:
      // if (this.value != null ? !this.value.equals(node.this.value) : node.this.value != null)
      // return false;
      // return true;
      
      return (this.value == null) ? (node.value == null) : this.value.equals(node.value);
    }
    
    public Set getNext() {
      return this.next;
    }
    
    public String getValue() {
      return this.value;
    }
    
    @Override
    public int hashCode() {
      return (this.value != null ? this.value.hashCode() : 0);
    }
    
    public void setNext(final Set set) {
      this.next = set;
    }
  }
  
  /**
   * This class is public so that {@link MethodUtils}, which is used by {@link ReflectiveDecomposer}
   * , can see it
   * @param <T> the type of {@link OC#getObject()}
   */
  public static class OC<T> {
    private final InnerClass foo;
    private final T object;
    private final OC<T> self;
    private Object writeOnly;
    
    public OC(final T object) {
      this.object = object;
      this.self = this;
      this.foo = new InnerClass();
    }
    
    public InnerClass getFoo() {
      return this.foo;
    }
    
    public T getObject() {
      return this.object;
    }
    
    public OC<T> getSelf() {
      return this.self;
    }
    
    public void setWriteOnly(final Object o) {
      this.writeOnly = o;
    }
    
    private static class InnerClass {
      private Object innerFoo;
      
      public Object getInnerFoo() {
        return this.innerFoo;
      }
    }
  }
}
