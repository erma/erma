package com.orbitz.monitoring.lib.interceptor;

import static org.mockito.Mockito.*;

import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @see TransactionMonitorInterceptor
 * @author Ray Krueger
 * @author Connor Garvey
 */
public class TransactionMonitorInterceptorTest extends TestCase {
    private void doTestCreateTransactionMonitor(final boolean prependClassName, final String name,
            final String expectedName) throws Exception {
        final Object invocationThis = new Object();
        final Class<?> invocationClass = invocationThis.getClass();
        final Method method = Object.class.getMethod("hashCode");
        final MonitoredAttributeSource monitoredAttributeSource = mock(MonitoredAttributeSource.class);
        final TransactionMonitorInterceptor interceptor = new TransactionMonitorInterceptor(
                monitoredAttributeSource);
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final MonitoredAttribute monitoredAttribute = mock(MonitoredAttribute.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getThis()).thenReturn(invocationThis);
        when(monitoredAttribute.getMonitorName()).thenReturn(name);
        doReturn(monitoredAttribute).when(monitoredAttributeSource).getMonitoredAttribute(method,
                invocationClass);
        interceptor.setPrependClassName(prependClassName);
        final TransactionMonitor monitor = interceptor.createTransactionMonitor(methodInvocation);
        assertEquals(expectedName, monitor.getAsString(TransactionMonitor.NAME));
    }
    
    /**
     * Tests TransactionMonitorInterceptor.createTransactionMonitor(MethodInvocation)
     * @throws Exception in case of failure
     */
    public void testCreateTransactionMonitorDoNotPrependClassName() throws Exception {
        final boolean prependClassName = false;
        final String name = "somethingunique";
        final String expectedName = name;
        doTestCreateTransactionMonitor(prependClassName, name, expectedName);
    }
    
    /**
     * Tests TransactionMonitorInterceptor.createTransactionMonitor(MethodInvocation)
     * @throws Exception in case of failure
     */
    public void testCreateTransactionMonitorDoNotPrependClassNameNullName() throws Exception {
        final boolean prependClassName = false;
        final String name = null;
        final String expectedName = "java.lang.Object.hashCode";
        doTestCreateTransactionMonitor(prependClassName, name, expectedName);
    }
    
    /**
     * Tests TransactionMonitorInterceptor.createTransactionMonitor(MethodInvocation)
     * @throws Exception in case of failure
     */
    public void testCreateTransactionMonitorPrependClassName() throws Exception {
        final boolean prependClassName = true;
        final String name = "somethingunique";
        final String expectedName = "java.lang.Object." + name;
        doTestCreateTransactionMonitor(prependClassName, name, expectedName);
    }
    
    /**
     * Tests TransactionMonitorInterceptor.createTransactionMonitor(MethodInvocation)
     * @throws Exception in case of failure
     */
    public void testCreateTransactionMonitorPrependClassNameNullName() throws Exception {
        final boolean prependClassName = true;
        final String name = null;
        final String expectedName = "java.lang.Object.hashCode";
        doTestCreateTransactionMonitor(prependClassName, name, expectedName);
    }
    
    /**
     * @see TransactionMonitorInterceptor#invoke(MethodInvocation)
     * @throws Throwable in case of failure
     */
    public void testInvokeSuccess() throws Throwable {
        
        final MockTransactionMonitor FIXED_MONITOR = new MockTransactionMonitor("FIXED_MONITOR");
        
        final TransactionMonitorInterceptor interceptor = new TransactionMonitorInterceptor(
                new MatchAlwaysMonitoredAttributeSource(true, true)) {
            
            @Override
            TransactionMonitor createTransactionMonitor(final MethodInvocation invocation) {
                assertEquals("testMethodOne", getMonitorName(invocation));
                return FIXED_MONITOR;
            }
        };
        
        final Object[] arguments = new Object[0];
        final Object rtn = interceptor.invoke(new MockMethodInvocation(arguments));
        
        assertEquals("OK", rtn);
        assertTrue(FIXED_MONITOR.isDone());
        assertTrue(FIXED_MONITOR.isSucceeded());
        assertFalse(FIXED_MONITOR.isFailed());
        assertNull(FIXED_MONITOR.getFailedDueTo());
        
        assertNotNull(FIXED_MONITOR.get("arguments"));
        assertEquals(arguments, FIXED_MONITOR.get("arguments"));
        assertEquals("OK", FIXED_MONITOR.get("result"));
        
    }
    
    /**
     * @see TransactionMonitorInterceptor#invoke(MethodInvocation)
     * @throws Throwable in case of failure
     */
    public void testInvokeSuccessFailure() throws Throwable {
        
        final MockTransactionMonitor FIXED_MONITOR = new MockTransactionMonitor("FIXED_MONITOR");
        
        final TransactionMonitorInterceptor interceptor = new TransactionMonitorInterceptor(
                new MatchAlwaysMonitoredAttributeSource(true, true)) {
            
            @Override
            TransactionMonitor createTransactionMonitor(final MethodInvocation invocation) {
                assertEquals("testMethodOne", getMonitorName(invocation));
                return FIXED_MONITOR;
            }
        };
        
        final Object[] arguments = new Object[0];
        try {
            interceptor.invoke(new MockMethodInvocation(arguments) {
                
                @Override
                public Object proceed() throws Throwable {
                    throw new IllegalArgumentException("Great googly moogly");
                }
            });
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException throwable) {
            // expected
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
        
        public MockMethodInvocation(final Object[] arguments) {
            this.arguments = arguments;
        }
        
        public Object[] getArguments() {
            return arguments;
        }
        
        public Method getMethod() {
            return ReflectionTarget.TEST_METHOD_ONE;
        }
        
        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException("Not implemented");
        }
        
        public Object getThis() {
            return null;
        }
        
        public Object proceed() throws Throwable {
            return "OK";
        }
    }
}
