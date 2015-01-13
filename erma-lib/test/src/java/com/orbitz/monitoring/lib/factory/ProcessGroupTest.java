package com.orbitz.monitoring.lib.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.test.MockMonitorProcessor;

/**
 * Tests {@link ProcessGroup}.
 * @author Doug Barth
 */
public class ProcessGroupTest {
  private static final String MONITOR_PROCESSOR_NAME = "monitorProcessorName";
  private ProcessGroup processGroup;
  
  @Before
  public void setUp() throws Exception {
    processGroup = new ProcessGroup(new MockMonitorProcessor());
  }
  
  private void assertElementsEqual(final Iterable<? extends Object> expected,
      final Iterable<? extends Object> actual) {
    if (!Iterables.elementsEqual(expected, actual)) {
      throw new AssertionFailedError("Expected " + Iterables.toString(expected) + " but was "
          + Iterables.toString(actual));
    }
  }
  
  private void assertSize(final String message, final int size, final Iterable<Object> iterable) {
    int count = 0;
    for (Object o : iterable) {
      count++;
    }
    if (count != size) {
      throw new AssertionFailedError(message == null ? "Expected size " + size + " but was "
          + count : message);
    }
  }
  
  private Monitor makeMonitor() {
    Monitor monitor = mock(Monitor.class);
    when(monitor.getLevel()).thenReturn(MonitoringLevel.ESSENTIAL);
    return monitor;
  }
  
  private ProcessGroup makeProcessGroup() {
    MonitoringEngine engine = mock(MonitoringEngine.class);
    MonitorProcessor processor = mock(MonitorProcessor.class);
    ProcessGroup group = spy(new ProcessGroup(processor));
    when(group.findMonitoringEngine()).thenReturn(engine);
    when(group.matchesExpressionFor(any(Monitor.class))).thenReturn(true);
    when(processor.getName()).thenReturn(MONITOR_PROCESSOR_NAME);
    when(engine.getProcessorLevel(MONITOR_PROCESSOR_NAME)).thenReturn(MonitoringLevel.INFO);
    return group;
  }
  
  @Test
  public void testDeactivate() {
    processGroup.setActive(false);
    assertFalse("Deactivated group should return 0 processors",
        processGroup.getProcessorsFor(new EventMonitor("foo")).iterator().hasNext());
  }
  
  /**
   * @see ProcessGroup#getProcessorsFor(Monitor)
   */
  @Test
  public void testGetProcessorsForInactive() {
    ProcessGroup group = makeProcessGroup();
    when(group.isActive()).thenReturn(false);
    assertEquals(Collections.emptyList(), group.getProcessorsFor(makeMonitor()));
  }
  
  /**
   * @see ProcessGroup#getProcessorsFor(Monitor)
   */
  @Test
  public void testGetProcessorsForGroupLevel() {
    ProcessGroup group = makeProcessGroup();
    when(group.findMonitoringEngine().getProcessorLevel(MONITOR_PROCESSOR_NAME)).thenReturn(null);
    assertElementsEqual(group.getAllProcessors(), group.getProcessorsFor(makeMonitor()));
  }
  
  /**
   * @see ProcessGroup#getProcessorsFor(Monitor)
   */
  @Test
  public void testGetProcessorsForLowerGroupLevel() {
    Monitor monitor = makeMonitor();
    ProcessGroup group = makeProcessGroup();
    when(group.findMonitoringEngine().getProcessorLevel(MONITOR_PROCESSOR_NAME)).thenReturn(null);
    when(monitor.getLevel()).thenReturn(MonitoringLevel.DEBUG);
    assertElementsEqual(Collections.emptyList(), group.getProcessorsFor(monitor));
  }
  
  /**
   * @see ProcessGroup#getProcessorsFor(Monitor)
   */
  @Test
  public void testGetProcessorsForLowerMonitorLevel() {
    ProcessGroup group = makeProcessGroup();
    Monitor monitor = makeMonitor();
    when(monitor.getLevel()).thenReturn(MonitoringLevel.DEBUG);
    assertElementsEqual(Collections.emptyList(), group.getProcessorsFor(monitor));
  }
  
  /**
   * @see ProcessGroup#getProcessorsFor(Monitor)
   */
  @Test
  public void testGetProcessorsForMonitorLevel() {
    ProcessGroup group = makeProcessGroup();
    assertElementsEqual(group.getAllProcessors(), group.getProcessorsFor(makeMonitor()));
  }
  
  /**
   * @see ProcessGroup#getProcessorsFor(Monitor)
   */
  @Test
  public void testGetProcessorsForMonitorDoesNotMatchExpression() {
    Monitor monitor = makeMonitor();
    ProcessGroup group = makeProcessGroup();
    when(group.matchesExpressionFor(monitor)).thenReturn(false);
    assertElementsEqual(Collections.emptyList(), group.getProcessorsFor(monitor));
  }
  
  @Test
  public void testMonitoringLevelNoLevelForProcessor() {
    ProcessGroup processGroup = new ProcessGroup(new MockMonitorProcessor());
    assertEquals("Default process group level should be INFO", MonitoringLevel.INFO.toString(),
        processGroup.getMonitoringLevel());
    
    EventMonitor event = new EventMonitor("baz", MonitoringLevel.DEBUG);
    
    Iterable processors = processGroup.getProcessorsFor(event);
    assertFalse("No processor should process this monitor b/c its level is DEBUG", processors
        .iterator().hasNext());
    
    EventMonitor event2 = new EventMonitor("baz", MonitoringLevel.INFO);
    
    processors = processGroup.getProcessorsFor(event2);
    
    assertSize("Processor should process this monitor b/c its level is INFO", 1, processors);
    
    EventMonitor event3 = new EventMonitor("baz", MonitoringLevel.ESSENTIAL);
    
    processors = processGroup.getProcessorsFor(event3);
    assertSize("Processor should process this monitor b/c its level is ESSENTIAL", 1, processors);
  }
  
  @Test
  public void testMonitoringLevelForProcessor() {
    MonitorProcessor mp = new MockMonitorProcessor("mpA");
    ProcessGroup processGroup = new ProcessGroup(mp);
    
    MonitoringEngine.getInstance().addProcessorLevel("mpA", MonitoringLevel.DEBUG);
    
    EventMonitor event = new EventMonitor("baz", MonitoringLevel.DEBUG);
    
    Iterable processors = processGroup.getProcessorsFor(event);
    assertSize("Processor should process this monitor b/c its level is DEBUG", 1, processors);
    
    MonitoringEngine.getInstance().addProcessorLevel("mpA", MonitoringLevel.ESSENTIAL);
    
    processors = processGroup.getProcessorsFor(event);
    assertSize("No processor should process this monitor b/c its level is DEBUG", 0, processors);
  }
  
  @Test
  public void testMonitoringLevelForProcessGroup() {
    ProcessGroup processGroup = new ProcessGroup(new MockMonitorProcessor());
    
    EventMonitor event = new EventMonitor("baz");
    
    Iterable processors = processGroup.getProcessorsFor(event);
    assertSize("ProcessorGroup should process INFO monitors by default", 1, processors);
    
    event = new EventMonitor("baz", MonitoringLevel.DEBUG);
    
    processors = processGroup.getProcessorsFor(event);
    assertSize("ProcessorGroup should not process DEBUG monitors by default", 0, processors);
    
    processGroup.updateMonitoringLevel(MonitoringLevel.DEBUG.toString());
    
    processors = processGroup.getProcessorsFor(event);
    assertSize("ProcessorGroup should apply this monitor b/c its level is DEBUG", 1, processors);
    
    event = new EventMonitor("baz", MonitoringLevel.ESSENTIAL);
    
    processors = processGroup.getProcessorsFor(event);
    assertSize("ProcessorGroup should apply this monitor b/c its level is DEBUG", 1, processors);
  }
  
  @Test
  public void testMonitoringLevelForProcessorAndProcessGroup() {
    MonitorProcessor mp = new MockMonitorProcessor("mpA");
    ProcessGroup processGroup = new ProcessGroup(mp);
    
    EventMonitor event = new EventMonitor("baz", MonitoringLevel.DEBUG);
    processGroup.updateMonitoringLevel(MonitoringLevel.DEBUG.toString());
    
    MonitoringEngine.getInstance().addProcessorLevel("mpA", MonitoringLevel.INFO);
    
    Iterable processors = processGroup.getProcessorsFor(event);
    assertSize("No processor should process this monitor b/c its level is DEBUG", 0, processors);
    
    event = new EventMonitor("baz", MonitoringLevel.DEBUG);
    processGroup.updateMonitoringLevel(MonitoringLevel.INFO.toString());
    
    MonitoringEngine.getInstance().addProcessorLevel("mpA", MonitoringLevel.DEBUG);
    
    processors = processGroup.getProcessorsFor(event);
    assertSize("Processor should process this monitor b/c its level is DEBUG", 1, processors);
  }
  
  @Test
  public void testNameMatching() {
    // Null name expression
    Iterable processors = processGroup.getProcessorsFor(new EventMonitor("foo"));
    assertSize("Processor should appy to monitor", 1, processors);
    processors = processGroup.getProcessorsFor(new EventMonitor("bar"));
    assertSize("Processor should appy to monitor", 1, processors);
    
    processGroup.setExpression("m.get('name').matches('.*')");
    processors = processGroup.getProcessorsFor(new EventMonitor("foo"));
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(new EventMonitor("bar"));
    assertSize("Processor should appy to monitor", 1, processors);
    
    processGroup.setExpression("m.get('name').matches('foo')");
    
    processors = processGroup.getProcessorsFor(new EventMonitor("foo"));
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(new EventMonitor("bar"));
    assertSize("Processor should appy to monitor", 0, processors);
  }
  
  @Test
  public void testUserDataMatching() {
    EventMonitor noUserData = new EventMonitor("noUserData");
    
    EventMonitor barUserData = new EventMonitor("barUserData");
    barUserData.set("foo", "bar");
    barUserData.set("bar", "baz");
    
    EventMonitor bazUserData = new EventMonitor("bazUserData");
    bazUserData.set("foo", "baz");
    
    // Null user data expression
    
    Iterable processors = processGroup.getProcessorsFor(noUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(barUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(bazUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processGroup.setExpression("m.get('foo').matches('.*')");
    processors = processGroup.getProcessorsFor(noUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processors = processGroup.getProcessorsFor(barUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(bazUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processGroup.setExpression("m.get('foo').matches('bar')");
    processors = processGroup.getProcessorsFor(noUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processors = processGroup.getProcessorsFor(barUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(bazUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processGroup.setExpression("m.get('bar').matches('bar')");
    processors = processGroup.getProcessorsFor(noUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processors = processGroup.getProcessorsFor(barUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processors = processGroup.getProcessorsFor(bazUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processGroup.setExpression("m.get('bar').matches('baz')");
    processors = processGroup.getProcessorsFor(noUserData);
    assertSize("Processor should appy to monitor", 0, processors);
    
    processors = processGroup.getProcessorsFor(barUserData);
    assertSize("Processor should appy to monitor", 1, processors);
    
    processors = processGroup.getProcessorsFor(bazUserData);
    assertSize("Processor should appy to monitor", 0, processors);
  }
  
  @Test
  public void testNonsenseMatching() {
    processGroup.setExpression("m.bar");
    
    Iterable processors = processGroup.getProcessorsFor(new EventMonitor("test"));
    assertSize("Processor should appy to monitor", 0, processors);
    
    processGroup.setExpression("m.name");
    
    processors = processGroup.getProcessorsFor(new EventMonitor("test"));
    assertSize("Processor should appy to monitor", 0, processors);
  }
}
