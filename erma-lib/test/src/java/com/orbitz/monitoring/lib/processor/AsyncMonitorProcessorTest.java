package com.orbitz.monitoring.lib.processor;

import static org.junit.Assert.*;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.lib.factory.ProcessGroup;
import com.orbitz.monitoring.lib.factory.SimpleMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link AsyncMonitorProcessor}
 * @author Doug Barth
 */
public class AsyncMonitorProcessorTest {
  private AsyncMonitorProcessor _processor;
  private MockMonitorProcessor _attachedProcessor;
  private BaseMonitoringEngineManager _monitoringEngineManager;
  
  /**
   * Prepares for each test
   */
  @Before
  public void setUp() {
    _attachedProcessor = new MockMonitorProcessor();
    _processor = new AsyncMonitorProcessor(new MonitorProcessor[] {_attachedProcessor});
    ProcessGroup processGroup = new ProcessGroup(_processor);
    SimpleMonitorProcessorFactory simpleMonitorProcessorFactory = new SimpleMonitorProcessorFactory(
        new ProcessGroup[] {processGroup});
    _monitoringEngineManager = new BaseMonitoringEngineManager(simpleMonitorProcessorFactory);
    _monitoringEngineManager.startup();
    _processor.startup();
  }
  
  /**
   * Cleans up after each test
   */
  @After
  public void tearDown() {
    _processor.shutdown();
    _monitoringEngineManager.shutdown();
  }
  
  /**
   * @see AsyncMonitorProcessor#monitorCreated(Monitor)
   * @see AsyncMonitorProcessor#flushEvents()
   */
  @Test
  public void testMonitorCreated() {
    EventMonitor event = new EventMonitor("test");
    _processor.monitorCreated(event);
    _processor.flushEvents();
    Monitor[] monitors = _attachedProcessor.extractMonitorCreatedObjects();
    boolean checkMonitorCreated = false;
    for (Monitor monitor : monitors) {
      if (monitor.get(Monitor.NAME).equals(event.get(Monitor.NAME))) {
        assertEquals(event.get(Monitor.CREATED_AT), monitor.get(Monitor.CREATED_AT));
        assertEquals(event.get(Monitor.THREAD_ID), monitor.get(Monitor.THREAD_ID));
        checkMonitorCreated = true;
      }
    }
    assertTrue(checkMonitorCreated);
  }
  
  /**
   * @see AsyncMonitorProcessor#process(Monitor)
   * @see AsyncMonitorProcessor#flushEvents()
   */
  @Test
  public void testProcess() {
    EventMonitor event = new EventMonitor("test");
    _processor.process(event);
    _processor.flushEvents();
    Monitor[] monitors = _attachedProcessor.extractProcessObjects();
    Arrays.asList(monitors);
    boolean checkMonitorCreated = false;
    for (Monitor monitor : monitors) {
      if (monitor.get(Monitor.NAME).equals(event.get(Monitor.NAME))) {
        assertEquals(event.get(Monitor.CREATED_AT), monitor.get(Monitor.CREATED_AT));
        assertEquals(event.get(Monitor.THREAD_ID), monitor.get(Monitor.THREAD_ID));
        checkMonitorCreated = true;
      }
    }
    assertTrue(checkMonitorCreated);
  }
}
