package com.orbitz.monitoring.api.template;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;

import junit.framework.TestCase;

/**
 * @author Ray Krueger
 */
public class TransactionMonitorTemplateTest extends TestCase {
    private MockMonitorProcessor _processor;
    
    public void setUp() {
        _processor = new MockMonitorProcessor();
        MockMonitorProcessorFactory factory = new MockMonitorProcessorFactory(_processor);
        
        MonitoringEngine.getInstance().setProcessorFactory(factory);
        MonitoringEngine.getInstance().setDecomposer(new MockDecomposer());
        MonitoringEngine.getInstance().restart();
    }
    
    public void testRuntimeExceptionThrown() throws Exception {
        TransactionMonitorTemplate template = new TransactionMonitorTemplate();

        try {

            template.doInMonitor(this.getClass(), "test", new TransactionMonitorCallback() {
                public Object doInMonitor(TransactionMonitor monitor) {
                    throw new UnsupportedOperationException("hi");
                }
            });

            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        TransactionMonitor tm = getWrappingMonitor();
        assertTrue(tm.getAsBoolean("failed"));
    }

    public void testErrorThrown() throws Exception {
        TransactionMonitorTemplate template = new TransactionMonitorTemplate();

        try {

            template.doInMonitor(this.getClass(), "test", new TransactionMonitorCallback() {
                public Object doInMonitor(TransactionMonitor monitor) {
                    throw new Error("hi");
                }
            });

            fail("Error expected");
        } catch (Error e) {
            // Expected
        }

        TransactionMonitor tm = getWrappingMonitor();
        assertTrue(tm.getAsBoolean("failed"));
    }

    public void testDefault() throws Exception {

        Object rtn = TransactionMonitorTemplate.INSTANCE.doInMonitor(this.getClass(), "test", new TransactionMonitorCallback() {

            public Object doInMonitor(TransactionMonitor monitor) {
                assertTrue(monitor.toString().indexOf(
                        "name=com.orbitz.monitoring.api.template.TransactionMonitorTemplateTest.test") != -1);
                return "Great googly moogly";
            }

        });

        assertEquals(rtn, "Great googly moogly");

    }

    public void testFailed() throws Exception {
        TransactionMonitor monitor = (TransactionMonitor) TransactionMonitorTemplate.INSTANCE.doInMonitor("test", new TransactionMonitorCallback() {
            public Object doInMonitor(TransactionMonitor monitor) {
                monitor.failed();
                return monitor;
            }
        });

        assertTrue(monitor.getAsBoolean("failed"));
    }

    public void testExample1() throws Exception {
        TransactionMonitorTemplate template = new TransactionMonitorTemplate();
        template.doInMonitor(this.getClass(), "monitorName", new TransactionMonitorCallback() {
            public Object doInMonitor(TransactionMonitor monitor) {
                return null;
            }
        });
    }

    public void testExample2() throws Exception {
        TransactionMonitorTemplate template = new TransactionMonitorTemplate();
        template.doInMonitor("monitorName", new TransactionMonitorCallback() {
            public Object doInMonitor(TransactionMonitor monitor) {
                return null;
            }
        });
    }

    public void testExample3() throws Exception {
        TransactionMonitorTemplate.INSTANCE.doInMonitor("MonitorName",
                new TransactionMonitorCallback() {
                    public Object doInMonitor(TransactionMonitor monitor) {
                        return null;
                    }
                }
        );
    }
    
    private TransactionMonitor getWrappingMonitor() {
        Monitor[] m = _processor.extractProcessObjects();
        
        assertNotNull(m[0]);
        return (TransactionMonitor) m[0];
    }
}
