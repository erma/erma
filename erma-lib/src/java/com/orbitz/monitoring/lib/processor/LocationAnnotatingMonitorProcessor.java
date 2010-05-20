package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;

/**
 * LocationAnnotatingMonitorProcessor adds an attribute named "stackTraceElement"
 * to the given Monitor.  The attribute's value is equal to StackTraceElement.toString()
 * for the first element that appears to identify the location of the ERMA API call.
 * We use the first element whose class name starts with "com.orbitz" but not
 * "com.orbitz.monitoring".
 *
 * @author Matt O'Keefe
 */
public class LocationAnnotatingMonitorProcessor extends MonitorProcessorAdapter {

    public void monitorCreated(Monitor monitor) {
                
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String attributeValue = null;
        for (StackTraceElement stackTraceElement : stackTrace) {
            String className = stackTraceElement.getClassName();
            if (className.startsWith("com.orbitz") && !className.startsWith("com.orbitz.monitoring")) {
                attributeValue = stackTraceElement.toString();
                break;
            }
        }
        if (attributeValue != null) {
            monitor.set("stackTraceElement", attributeValue);
        }
    }

}
