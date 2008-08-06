package com.orbitz.monitoring.lib.renderer;

import com.orbitz.monitoring.api.Monitor;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Renders a monitor and all of its attributes to a human readable format.
 * <p/>
 */
public class SimpleMonitorRenderer {

    /**
     * Renders a Monitor in XML format.
     *
     * @param monitor monitor to render
     * @return a String of XML
     */
    public String renderMonitor(Monitor monitor) {

        if (monitor == null) {
            return "null";
        }

        StringBuffer buf = new StringBuffer(monitor.getClass().getName());

        Map attributeMap = monitor.getAll();

        buf.append(renderMonitor(attributeMap, false));

        return buf.toString();
    }

    /**
     * Renders a Monitor in XML format.
     *
     * @param monitor monitor to render
     * @param includeStackTraces true if every Throwable attribute value should render with a stack trace
     * @return a String of XML
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

    /**
     * Renders an attribute map in xml format, will not contain the monitor's classname
     * @param attributeMap
     * @param includeStackTraces true if every Throwable attribute value should render with a stack trace
     * @return a string representation of the attribute map in xml format
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
            if (includeStackTraces && Throwable.class.isAssignableFrom(v.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ((Throwable)v).printStackTrace(pw);
                buf.append(sw.toString());
            } else {
                buf.append(v);
            }
        }

        return buf.toString();
    }
}
