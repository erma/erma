package com.orbitz.monitoring.lib;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.engine.StackBasedInheritableStrategy;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.lib.renderer.XmlMonitorRenderer;

/**
 * Tests {@link BaseMonitoringEngineManager}
 * @author Matt O'Keefe
 */
public class BaseMonitoringEngineManagerTest extends TestCase {

  BaseMonitoringEngineManager manager;

  @Override
  protected void setUp() throws Exception {
      manager = new BaseMonitoringEngineManager();
      manager.startup();
      manager.reload();
  }

  @Override
  protected void tearDown() throws Exception {
      manager.shutdown();
  }

/**
   * @see BaseMonitoringEngineManager#startup()
   */
  public void testEPMLevels() {
    List<String> attributeList = new ArrayList<String>();
    attributeList.add("name");
    XmlMonitorRenderer renderer = new XmlMonitorRenderer(attributeList);
    renderer.setPrettyPrint(true);

    {
      TransactionMonitor txn = new TransactionMonitor("foo");
      EventMonitor m1 = new EventMonitor("bar");
      m1.fire();
      EventMonitor m2 = new EventMonitor("baz");
      m2.fire();
      txn.done();
      assertEquals("<TransactionMonitor name=\"foo\">\n" + "  <EventMonitor name=\"bar\"/>\n"
          + "  <EventMonitor name=\"baz\"/>\n" + "</TransactionMonitor>",
          renderer.renderMonitor(txn));
    }
    StackBasedInheritableStrategy.class.cast(
        manager.getInheritableStrategy()).setEventPatternLevel(
        MonitoringLevel.ESSENTIAL);
    {
      TransactionMonitor txn = new TransactionMonitor("foo", MonitoringLevel.ESSENTIAL);
      EventMonitor m1 = new EventMonitor("bar");
      m1.fire();
      EventMonitor m2 = new EventMonitor("baz");
      m2.fire();
      txn.done();
      assertEquals("<TransactionMonitor name=\"foo\"/>", renderer.renderMonitor(txn));
    }
  }
  
  /**
   * @see BaseMonitoringEngineManager#startup()
   */
  public void testMaliciousRuntimeControls() {
    int i = 0;
    try {
      while (true) {
        // prefixing the name with "zz" so it won't interfere with other level tests
        manager.updateLevelForMonitor("zz" + Integer.toHexString(i++), "DEBUG");
      }
    }
    catch (RuntimeException e) {
      // expected
    }
    catch (OutOfMemoryError e) {
      fail("RuntimeException should have been thrown");
    }
  }
}
