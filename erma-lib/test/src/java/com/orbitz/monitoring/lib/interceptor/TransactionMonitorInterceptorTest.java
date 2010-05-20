package com.orbitz.monitoring.lib.interceptor;

import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @author Ray Krueger
 */
public class TransactionMonitorInterceptorTest extends TestCase {

    public void testSuccess() throws Throwable {

        final MockTransactionMonitor FIXED_MONITOR = new MockTransactionMonitor("FIXED_MONITOR");

        TransactionMonitorInterceptor interceptor = new TransactionMonitorInterceptor(
                new MatchAlwaysMonitoredAttributeSource(true, true)) {

            protected TransactionMonitor createTransactionMonitor(MethodInvocation invocation) {
                assertEquals("testMethodOne", getMonitorName(invocation));
                return FIXED_MONITOR;
            }
        };

        Object[] arguments = new Object[0];
        Object rtn = interceptor.invoke(new MockMethodInvocation(arguments));

        assertEquals("OK", rtn);
        assertTrue(FIXED_MONITOR.isDone());
        assertTrue(FIXED_MONITOR.isSucceeded());
        assertFalse(FIXED_MONITOR.isFailed());
        assertNull(FIXED_MONITOR.getFailedDueTo());

        assertNotNull(FIXED_MONITOR.get("arguments"));
        assertEquals(arguments, FIXED_MONITOR.get("arguments"));
        assertEquals("OK", FIXED_MONITOR.get("result"));

    }

    public void testSuccessFailure() throws Throwable {

        final MockTransactionMonitor FIXED_MONITOR = new MockTransactionMonitor("FIXED_MONITOR");

        TransactionMonitorInterceptor interceptor = new TransactionMonitorInterceptor(
                new MatchAlwaysMonitoredAttributeSource(true, true)) {

            protected TransactionMonitor createTransactionMonitor(MethodInvocation invocation) {
                assertEquals("testMethodOne", getMonitorName(invocation));
                return FIXED_MONITOR;
            }
        };

        Object[] arguments = new Object[0];
        try {
            interceptor.invoke(new MockMethodInvocation(arguments){

                public Object proceed() throws Throwable {
                    throw new IllegalArgumentException("Great googly moogly");
                }
            });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException throwable) {
            //expected
        }


        assertTrue(FIXED_MONITOR.isDone());
        assertFalse(FIXED_MONITOR.isSucceeded());
        assertTrue(FIXED_MONITOR.isFailed());
        assertSame(arguments, FIXED_MONITOR.get("arguments"));
        assertEquals(IllegalArgumentException.class, FIXED_MONITOR.getFailedDueTo().getClass());
        assertEquals("Great googly moogly", FIXED_MONITOR.getFailedDueTo().getMessage());
    }


    private static class MockMethodInvocation implements MethodInvocation {

        private final Object[] arguments;


        public MockMethodInvocation(Object[] arguments) {
            this.arguments = arguments;
        }

        public Method getMethod() {
            return ReflectionTarget.TEST_METHOD_ONE;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public Object proceed() throws Throwable {
            return "OK";
        }

        public Object getThis() {
            return null;
        }

        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
