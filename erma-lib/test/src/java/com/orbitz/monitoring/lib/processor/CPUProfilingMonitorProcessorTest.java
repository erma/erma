package com.orbitz.monitoring.lib.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Tests {@link CPUProfilingMonitorProcessor}
 * @author Matt O'Keefe
 */
public class CPUProfilingMonitorProcessorTest {
  CPUProfilingMonitorProcessor cpuProfilingMonitorProcessor = new CPUProfilingMonitorProcessor();
  
  @Test
  public void testStartOffNonTransaction() {
      Monitor m = new AbstractMonitor(){};
      cpuProfilingMonitorProcessor.setEnabled(false);
      int attrCountBefore = m.getAll().size();
      
      cpuProfilingMonitorProcessor.monitorStarted(m);
      
      assertEquals(attrCountBefore, m.getAll().size());
  }
  
  @Test
  public void testStartOnNonTransaction() {
      Monitor m = new AbstractMonitor(){};
      cpuProfilingMonitorProcessor.setEnabled(true);
      int attrCountBefore = m.getAll().size();

      cpuProfilingMonitorProcessor.monitorStarted(m);
      
      assertEquals(attrCountBefore, m.getAll().size());
  }

  @Test
  public void testStartOff() {
      Monitor m = new TransactionMonitor("T1");
      cpuProfilingMonitorProcessor.setEnabled(false);
      int attrCountBefore = m.getAll().size();

      cpuProfilingMonitorProcessor.monitorStarted(m);
      
      assertEquals(attrCountBefore, m.getAll().size());
  }
  
  @Test
  public void testStart() {
      Monitor m = new TransactionMonitor("T1");
      cpuProfilingMonitorProcessor.setEnabled(true);

      cpuProfilingMonitorProcessor.monitorStarted(m);
      
      assertTrue(m.hasAttribute("startCPUTime"));
  }

  @Test
  public void testProcessOffNonTransaction() {
      Monitor m = new AbstractMonitor(){};
      cpuProfilingMonitorProcessor.setEnabled(false);
      int attrCountBefore = m.getAll().size();
      
      cpuProfilingMonitorProcessor.process(m);
      
      assertEquals(attrCountBefore, m.getAll().size());
  }
  
  @Test
  public void testProcessOnNonTransaction() {
      Monitor m = new AbstractMonitor(){};
      cpuProfilingMonitorProcessor.setEnabled(true);
      int attrCountBefore = m.getAll().size();

      cpuProfilingMonitorProcessor.process(m);
      
      assertEquals(attrCountBefore, m.getAll().size());
  }

  @Test
  public void testProcessOff() {
      Monitor m = new TransactionMonitor("T1");
      cpuProfilingMonitorProcessor.setEnabled(false);
      int attrCountBefore = m.getAll().size();

      cpuProfilingMonitorProcessor.process(m);
      
      assertEquals(attrCountBefore, m.getAll().size());
  }
  
  @Test
  public void testProcessNeverStarted() {
      TestAppender appender = new TestAppender();
      Logger logger = Logger.getLogger(CPUProfilingMonitorProcessor.class.getName());
      logger.addAppender(appender);
      logger.setLevel(Level.ALL);
      Monitor m = new TransactionMonitor("T1");
      cpuProfilingMonitorProcessor.setEnabled(true);

      cpuProfilingMonitorProcessor.process(m);
      
      assertFalse(m.hasAttribute("endCPUTime"));
      
      assertEquals(1, appender.getEvents().size());
      LoggingEvent loggingEvent = appender.getEvents().get(0);
      assertEquals(Level.WARN, loggingEvent.getLevel());
      assertEquals("No startCPUTime for Monitor named T1", loggingEvent.getMessage());
  }
  
  @Test
  public void testProcess() {
      Monitor m = new TransactionMonitor("T1");
      cpuProfilingMonitorProcessor.setEnabled(true);
      cpuProfilingMonitorProcessor.monitorStarted(m);

      consumeCPU();

      cpuProfilingMonitorProcessor.process(m);
      
      assertTrue(m.hasAttribute("endCPUTime"));
      assertTrue(m.hasAttribute("cpuTimeMillis"));

      long cpuTimeMillis = m.getAsLong("cpuTimeMillis");
      assertTrue("CPU time apparently not measured", (cpuTimeMillis > 1));
  }

  private void consumeCPU() {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() < start + 500) {
    }
  }
}
