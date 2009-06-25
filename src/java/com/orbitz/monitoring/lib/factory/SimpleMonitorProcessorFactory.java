package com.orbitz.monitoring.lib.factory;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An implementation of MonitorProcessorFactory that needs to be conifgured
 * programmatically.
 *
 * @author Doug Barth
 */
public class SimpleMonitorProcessorFactory implements MonitorProcessorFactory {
    // ** PRIVATE DATA ********************************************************
    private ProcessGroup[] _processGroups;

    // ** CONSTRUCTORS ********************************************************
    public SimpleMonitorProcessorFactory(ProcessGroup[] processGroups) {
        if (processGroups == null) {
            processGroups = new ProcessGroup[0];
        }
        _processGroups = processGroups;
    }

    // ** PUBLIC METHODS ******************************************************
    public void startup() {
        Set allMps = getAllMonitorProcessors();
        for (Iterator i = allMps.iterator(); i.hasNext();) {
            MonitorProcessor processor = (MonitorProcessor) i.next();
            processor.startup();
        }
    }

    public void shutdown() {
        Set allMps = getAllMonitorProcessors();
        for (Iterator i = allMps.iterator(); i.hasNext();) {
            MonitorProcessor processor = (MonitorProcessor) i.next();
            processor.shutdown();
        }
    }

    public MonitorProcessor[] getProcessorsForMonitor(Monitor monitor) {
        Set applicableProcessors = new LinkedHashSet();

        for (int i = 0; i < _processGroups.length; i++) {
           ProcessGroup processGroup = _processGroups[i];
           applicableProcessors.addAll(processGroup.getProcessorsFor(monitor));
        }

        return (MonitorProcessor[]) applicableProcessors.toArray(
                new MonitorProcessor[applicableProcessors.size()]);
    }

    public MonitorProcessor[] getAllProcessors() {
        Set allMps = getAllMonitorProcessors();

        return (MonitorProcessor[])allMps.toArray(new MonitorProcessor[allMps.size()]);
    }

    public Set getAllMonitorProcessors() {
        Set allMps = new LinkedHashSet();
        for (int i = 0; i < _processGroups.length; i++) {
            ProcessGroup processGroup = _processGroups[i];
            allMps.addAll(Arrays.asList(processGroup.getProcessors()));
        }

        return allMps;
    }
}
