package com.orbitz.monitoring.lib.interceptor;

import com.orbitz.monitoring.api.annotation.Monitored;

import java.lang.reflect.Method;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class ReflectionTarget {

    public static final Method TEST_METHOD_ONE;
    public static final Method TEST_METHOD_TWO;
    public static final Method TEST_METHOD_THREE;

    public static final Method method_level_default;
    public static final Method method_level_debug;
    public static final Method method_level_essential;

    static {
        try {
            TEST_METHOD_ONE = ReflectionTarget.class.getMethod("testMethodOne", new Class[0]);
            TEST_METHOD_TWO = ReflectionTarget.class.getMethod("testMethodTwo", new Class[0]);
            TEST_METHOD_THREE = ReflectionTarget.class.getMethod("testMethodThree", new Class[0]);

            method_level_default = ReflectionTarget.class.getMethod("methodDefault", new Class[0]);
            method_level_debug = ReflectionTarget.class.getMethod("methodDebug", new Class[0]);
            method_level_essential = ReflectionTarget.class.getMethod("methodEssential", new Class[0]);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void testMethodOne() {

    }

    public void testMethodTwo() {

    }

    public void testMethodThree() throws Exception {

    }

    @Monitored
    public void methodDefault() {

    }

    @Monitored(levelStr="debug")
    public void methodDebug() {

    }

    @Monitored(levelStr="essential")
    public void methodEssential() {

    }
}
