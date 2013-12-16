package com.orbitz.monitoring.lib.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.test.MockMonitorProcessor;

/**
 * Tests {@link AsyncMonitorProcessor}
 * @author Doug Barth
 */
public class AsyncMonitorProcessorTest {
  private AsyncMonitorProcessor _processor;
  private MockMonitorProcessor _attachedProcessor;
  
  /**
   * Prepares for each test
   */
  @Before
  public void setUp() {
    _attachedProcessor = new MockMonitorProcessor();
    _processor = new AsyncMonitorProcessor(new MonitorProcessor[] {_attachedProcessor});
    _processor.startup();
  }
  
  /**
   * Cleans up after each test
   */
  @After
  public void tearDown() {
    _processor.shutdown();
  }
  
  @Test
  public void testMonitorCreated() {
      final Monitor event = new AbstractMonitor() {
        SerializableMonitor innerMon = new SerializableMonitor(null);
        @Override
        public SerializableMonitor getSerializableMomento() {
            return innerMon;
        }
      };
      AsyncMonitorProcessor processor = new AsyncMonitorProcessor(new MonitorProcessorAdapter() {
        @Override
        public void monitorCreated(Monitor monitor) {
            assertSame(event.getSerializableMomento(), monitor);
            monitor.set("processed", true);
        }
      });
      processor.startup();
      processor.monitorCreated(event);
      processor.flushEvents();
      assertTrue(event.getSerializableMomento().getAsBoolean("processed"));
  }
  
  @Test
  public void testMonitorStarted() {
      final Monitor event = new AbstractMonitor() {
        SerializableMonitor innerMon = new SerializableMonitor(null);
        @Override
        public SerializableMonitor getSerializableMomento() {
            return innerMon;
        }
      };
      AsyncMonitorProcessor processor = new AsyncMonitorProcessor(new MonitorProcessorAdapter() {
        @Override
        public void monitorStarted(Monitor monitor) {
            assertSame(event.getSerializableMomento(), monitor);
            monitor.set("processed", true);
        }
      });
      processor.startup();
      processor.monitorStarted(event);
      processor.flushEvents();
      assertTrue(event.getSerializableMomento().getAsBoolean("processed"));
  }
  
  @Test
  public void testProcess() {
      final Monitor event = new AbstractMonitor() {
          SerializableMonitor innerMon = new SerializableMonitor(null);
          @Override
          public SerializableMonitor getSerializableMomento() {
              return innerMon;
          }
        };
        AsyncMonitorProcessor processor = new AsyncMonitorProcessor(new MonitorProcessorAdapter() {
          @Override
          public void process(Monitor monitor) {
              assertSame(event.getSerializableMomento(), monitor);
              monitor.set("processed", true);
          }
        });
        processor.startup();
        processor.process(event);
        processor.flushEvents();
        assertTrue(event.getSerializableMomento().getAsBoolean("processed"));
  }
  
  @Test
  public void testSlowShutDown() {
      final long wait = 1;
      final Monitor event = new AbstractMonitor() {
          SerializableMonitor innerMon = new SerializableMonitor(null);
          @Override
          public SerializableMonitor getSerializableMomento() {
              return innerMon;
          }
        };
        AsyncMonitorProcessor processor = new AsyncMonitorProcessor(new MonitorProcessorAdapter() {
          @Override
          public void process(Monitor monitor) {
              assertSame(event.getSerializableMomento(), monitor);
              try {
                Thread.yield();
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                fail("Should not have been interupted");
            }
              monitor.set("processed", true);
          }
        });
        processor.startup();
        long start = System.currentTimeMillis();
        processor.process(event);
        if ((System.currentTimeMillis() - start) <= wait) {
            assertFalse(event.getSerializableMomento().hasAttribute("processed"));
        } else {
            System.out.println("I never got control before the processor");
        }
        processor.flushEvents();
        assertTrue((System.currentTimeMillis() - start) >= wait);
        assertTrue(event.getSerializableMomento().getAsBoolean("processed"));
  }
}
