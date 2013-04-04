package com.orbitz.monitoring.lib.decomposer;

import static org.junit.Assert.*;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import com.orbitz.monitoring.api.monitor.EventMonitor;
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
    assertNotNull(deepObject.writeOnly);
    assertNotNull(deepObject.privateInnerClass);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("deepObject", _decomposer.decompose(deepObject));
    final DynaBean actual = (DynaBean)monitor.get("deepObject");
    final DynaBean subActual = (DynaBean)actual.get("object");
    final DynaBean subSubActual = (DynaBean)subActual.get("object");
    String value = ((String)subSubActual.get("object"));
    assertEquals("foo", value);
    assertNull(actual.get("writeOnly"));
    assertEquals(null, ((DynaBean)actual.get("privateInnerClass")).get("value"));
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
    Set<String> set = Sets.newHashSet("foo");
    Serializable decomposed = _decomposer.decompose(set);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("set", decomposed);
    assertTrue(Sets.difference(set, monitor.getAsSet("set")).size() == 0);
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testEqualsVsIdentity() {
    Node a1 = new Node("a");
    Node a2 = new Node("a");
    a1.addNext(a2);
    assertEquals(a1, a2);
    assertNotSame(a1, a2);
    EventMonitor monitor = new EventMonitor("test");
    monitor.set("node", _decomposer.decompose(a1));
    final DynaBean node = (DynaBean)monitor.get("node");
    assertEquals("a", node.get("value"));
    assertEquals("a", ((Set<DynaBean>)node.get("next")).iterator().next().get("value"));
  }
  
  /**
   * @see AttributeDecomposer#decompose(Object)
   */
  @Test
  public void testStringBufferSupport() {
    StringBuffer buffer = new StringBuffer("abc");
    String decomposed = (String)_decomposer.decompose(buffer);
    assertEquals(buffer.toString(), decomposed);
  }
  
  /**
   * This class is public so that {@link MethodUtils}, which is used by ReflectiveDecomposer, can
   * see it
   */
  public static class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    private Set<Node> next = new HashSet<Node>();
    private final String value;
    
    /**
     * Creates a node with the specified value
     * @param value the value of the node
     */
    public Node(final String value) {
      this.value = value;
    }
    
    /**
     * Adds a node to the collection of next nodes for this node
     * @param next the node to add
     */
    public void addNext(final Node next) {
      this.next.add(next);
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
      if (o instanceof Node) {
        Node that = (Node)o;
        return Objects.equal(this.value, that.value);
      }
      return false;
    }
    
    /**
     * Gets the collection of next nodes
     * @return the next nodes
     */
    public Set<Node> getNext() {
      return this.next;
    }
    
    /**
     * Gets the value of this node
     * @return the value
     */
    public String getValue() {
      return this.value;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return Objects.hashCode(this.value);
    }
    
    /**
     * Sets the collection of next nodes
     * @param set the next nodes to set
     */
    public void setNext(final Set<Node> set) {
      Preconditions.checkNotNull(set);
      this.next = set;
    }
  }
  
  /**
   * An object container<br>
   * This class is public so that {@link MethodUtils}, which is used by ReflectiveDecomposer, can
   * see it
   * @param <T> the type of {@link OC#getObject()}
   */
  public static class OC<T> {
    private final InnerClass privateInnerClass;
    private final T object;
    private final OC<T> self;
    private Object writeOnly;
    
    /**
     * Creates a new object container
     * @param object
     */
    public OC(final T object) {
      this.object = object;
      this.self = this;
      this.privateInnerClass = new InnerClass();
      this.writeOnly = new Object();
    }
    
    /**
     * Gets a value with a class that is private
     * @return the private class value
     */
    public InnerClass getPrivateInnerClass() {
      return this.privateInnerClass;
    }
    
    /**
     * Gets the value of this container
     * @return the value
     */
    public T getObject() {
      return this.object;
    }
    
    /**
     * Gets a reference to this container, to ensure that cyclic references are ignored
     * @return a reference to this
     */
    public OC<T> getSelf() {
      return this.self;
    }
    
    /**
     * Sets a value that cannot be gotten
     * @param o the value to set
     */
    public void setWriteOnly(final Object o) {
      this.writeOnly = o;
    }
    
    private static class InnerClass {
      private String value;
      
      public InnerClass() {
        this.value = "hello";
      }
      
      @SuppressWarnings("unused")
      public String getValue() {
        return this.value;
      }
      
      @SuppressWarnings("unused")
      public void setValue(final String value) {
        this.value = value;
      }
    }
  }
}
