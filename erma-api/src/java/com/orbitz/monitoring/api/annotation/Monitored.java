package com.orbitz.monitoring.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a class or method should be monitored.
 * <p/>
 * The <i>value</i> of this annotation will be used as the monitor name.
 * <p/>
 * If no value is provided, the intercepted method name will be used.
 * <p/>
 * Any value included when annotating a type will be ignored.
 *
 * @author Ray Krueger
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Monitored {

    /**
     * The name to use for the monitor.
     * If nothing is specified then the method name will be used.
     */
    String value() default "";

    /**
     * A string representation of the MonitoringLevel.
     * If no value is specified, default is INFO.
     */
    String levelStr() default "info";

    /**
     * Should the result of the intercepted method be included in the Monitor attributes?
     * If includeResult is true, then the result will be set on the Monitor using <i>result</i> as the key.
     * <b>Defaults to false.</b>
     */
    boolean includeResult() default false;

    /**
     * Should the intercepted method arguments be set on the monitor?
     * If true, then the arguments will be set, as is, on the monitor using <i>arguments</i> as the key.
     * <b>Defaults to false</b>
     */
    boolean includeArguments() default false;
}
