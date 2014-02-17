package com.orbitz.monitoring.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares that a class or method should be monitored.
 * </p>
 * <p>
 * The <i>value</i> property will be used as the monitor name. If a value is not provided, the
 * intercepted method name will be used. Any value included when annotating a type will be ignored.
 * </p>
 * @author Ray Krueger
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Monitored {
    
    /**
     * The name to use for the monitor. If a value is not specified, the method name will be used.<br />
     * TODO: Version 5: Change the default to null
     */
    String value() default "";
    
    /**
     * A string representation of the {@link com.orbitz.monitoring.api.MonitoringLevel}. The default is "info".<br/>
     * FIXME: Version 5: Change this to "INFO" to match {@link com.orbitz.monitoring.api.MonitoringLevel#INFO}
     * @return the level name
     */
    String levelStr() default "info";
    
    /**
     * Indicates whether the result of the intercepted method will be included in the Monitor
     * attributes. If includeResult is true, then the result will be set on the Monitor using
     * <i>result</i> as the key. <b>Defaults to false.</b>
     * @return true if return values should be set into monitors, false otherwise
     */
    boolean includeResult() default false;
    
    /**
     * Indicates whether the intercepted method's arguments will be set on the monitor. If true,
     * then the arguments will be set on the monitor using <i>arguments</i> as the key. <b>Defaults
     * to false</b>
     * @return true if arguments should be set into monitors, false otherwise
     */
    boolean includeArguments() default false;
}
