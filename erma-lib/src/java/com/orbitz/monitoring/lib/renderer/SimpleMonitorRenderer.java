package com.orbitz.monitoring.lib.renderer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.orbitz.monitoring.api.Monitor;

/**
 * A simple renderer that renders a monitor with each attribute on a separate line.
 * <p>
 * Example:<br>
 * 
 * <pre>
 * com.orbitz.monitoring.api.monitor.TransactionMonitor
 *     -&gt; arguments = {blah,java.lang.Object@93b022,12345}
 *     -&gt; createdAt = Fri Nov 28 15:09:37 EST 2008
 *     -&gt; endTime = Fri Nov 28 15:09:37 EST 2008
 *     -&gt; failed = false
 *     -&gt; latency = 0
 *     -&gt; name = com.example.MyClass.myMethod
 *     -&gt; sequenceId = m
 *     -&gt; startTime = Fri Nov 28 15:09:37 EST 2008
 *     -&gt; threadId = b92dc2
 * </pre>
 * 
 * </p>
 */
public class SimpleMonitorRenderer implements MonitorRenderer {
    private ToStringStyle toStringStyle = ToStringStyle.SIMPLE_STYLE;

    public ToStringStyle getToStringStyle() {
        return toStringStyle;
    }

    /**
     * Renders a Monitor in a multiple-line String format.
     * @param monitor monitor to render
     * @return a String representation of the monitor.
     */
    public String renderMonitor(Monitor monitor) {
        return renderMonitor(monitor, false);
    }

    /**
     * Renders a Monitor in a multiple-line String format.
     * @param monitor monitor to render
     * @param includeStackTraces true if every Throwable attribute value should render with a stack trace
     * @return a String representation of the monitor.
     */
    public String renderMonitor(Monitor monitor, boolean includeStackTraces) {

        if (monitor == null) {
            return "null";
        }

        StringBuffer buf = new StringBuffer(monitor.getClass().getName());

        Map attributeMap = monitor.getAll();

        buf.append(renderMonitor(attributeMap, includeStackTraces));

        return buf.toString();
    }

    public void setToStringStyle(ToStringStyle toStringStyle) {
        this.toStringStyle = toStringStyle;
    }

    private ToStringBuilder createToStringBuilder(Object v) {
        return new ToStringBuilder(v, toStringStyle);
    }

    /**
     * Renders an attribute map in xml format, will not contain the monitor's classname
     * @param attributeMap
     * @param includeStackTraces true if every Throwable attribute value should render with a stack trace
     * @return a string representation of the attribute map in multiple-line format
     */
    private String renderMonitor(Map attributeMap, boolean includeStackTraces) {
        if (attributeMap == null) {
            return "null";
        }

        StringBuffer buf = new StringBuffer();

        Map allAttributes = new TreeMap(attributeMap);
        for (Iterator i = allAttributes.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            buf.append("\n\t-> ").append(e.getKey()).append(" = ");

            Object v = e.getValue();
            if (shouldHandleStackTraces(includeStackTraces, v)) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ((Throwable) v).printStackTrace(pw);
                buf.append(sw.toString());
            } else {
                buf.append(createToStringBuilder(v).append(v));
            }
        }

        return buf.toString();
    }

    private boolean shouldHandleStackTraces(boolean includeStackTraces, Object v) {
        return includeStackTraces && Throwable.class.isAssignableFrom(v.getClass());
    }
}
