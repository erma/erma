package com.orbitz.monitoring.lib.processor;

import static org.junit.Assert.*;

import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link CPUProfilingMonitorProcessor}
 * @author Matt O'Keefe
 */
public class CPUProfilingMonitorProcessorTest {
  CPUProfilingMonitorProcessor cpuProfilingMonitorProcessor = new CPUProfilingMonitorProcessor();
  private final MockMonitorProcessor mockMonitorProcessor = new MockMonitorProcessor();
  
  /**
   * Prepares for tests
   * @throws Exception in case of failure
   */
  @Before
  public void setUp() throws Exception {
    cpuProfilingMonitorProcessor.setEnabled(true);
    MonitorProcessor[] processors = new MonitorProcessor[2];
    processors[0] = cpuProfilingMonitorProcessor;
    processors[1] = mockMonitorProcessor;
    MonitoringEngine.getInstance().setProcessorFactory(new MockMonitorProcessorFactory(processors));
    MonitoringEngine.getInstance().setDecomposer(new MockDecomposer());
    MonitoringEngine.getInstance().startup();
  }
  
  /**
   * Cleans up after tests
   * @throws Exception in case of failure
   */
  @After
  public void tearDown() throws Exception {
    MonitoringEngine.getInstance().shutdown();
  }
  
  /**
   * @see CPUProfilingMonitorProcessor
   */
  @Test
  public void testConcurrency() {
    TransactionMonitor monitor = new TransactionMonitor("Test");
    consumeCPU();
    monitor.done();
    long cpuTimeMillis = monitor.getAsLong("cpuTimeMillis");
    assertTrue("CPU time apparently not measured", (cpuTimeMillis > 1));
  }
  
  private void consumeCPU() {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() < start + 500) {
    }
  }
}
