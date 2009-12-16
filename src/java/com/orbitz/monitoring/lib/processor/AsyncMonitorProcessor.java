package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorAttachable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A monitor processor that processes monitors on a separate thread. This
 * is recommended for use when decoupling the processing of monitors from the
 * gathering of the data is allowed.
 *
 * @author Doug Barth
 */
public final class AsyncMonitorProcessor
        implements MonitorProcessor, MonitorProcessorAttachable {
    // ** PRIVATE DATA ********************************************************
    private String _name;
    private List _processors;
    private ExecutorService _monitorProcessingExecutor;

    // ** CONSTRUCTORS ********************************************************
    public AsyncMonitorProcessor() {
        _processors = new LinkedList();
    }

    /**
     * Constructor that supports DI.
     * 
     * @since 3.5
     *
     * @param processors
     */
    public AsyncMonitorProcessor(MonitorProcessor[] processors) {
        this();
        if(processors != null && processors.length > 0) {
            _processors = Arrays.asList(processors);
        }
    }

    // ** PUBLIC METHODS ******************************************************
    public void startup() {
        _monitorProcessingExecutor = Executors.newSingleThreadExecutor();
        for(Iterator i = _processors.iterator(); i.hasNext();) {
            ((MonitorProcessor) i.next()).startup();
        }
    }

    public void shutdown() {
        flushEvents();
        _monitorProcessingExecutor.shutdownNow();
        for(Iterator i = _processors.iterator(); i.hasNext();) {
            ((MonitorProcessor) i.next()).shutdown();
        }
    }

    public void monitorCreated(Monitor monitor) {
        _monitorProcessingExecutor.execute(new MonitorProcessBundle(
                    monitor.getSerializableMomento()) {
            protected void processWithProcessor(MonitorProcessor processor) {
                processor.monitorCreated(_monitor);
            }
        });
    }

    public void monitorStarted(Monitor monitor) {
        _monitorProcessingExecutor.execute(new MonitorProcessBundle(
                    monitor.getSerializableMomento()) {
            protected void processWithProcessor(MonitorProcessor processor) {
                processor.monitorStarted(_monitor);
            }
        });
    }

    public void process(Monitor monitor) {
        _monitorProcessingExecutor.execute(new MonitorProcessBundle(
                    monitor.getSerializableMomento()) {
            protected void processWithProcessor(MonitorProcessor processor) {
                processor.process(_monitor);
            }
        });
    }

    public void flushEvents() {
        _monitorProcessingExecutor.shutdown();
        while (! _monitorProcessingExecutor.isTerminated()) {
            try {
                _monitorProcessingExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        _monitorProcessingExecutor = Executors.newSingleThreadExecutor();
    }

    public void addMonitorProcessor(MonitorProcessor processor) {
        _processors.add(processor);
    }

    public List getMonitorProcessors() {
        return _processors;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    // ** INNER CLASSES *******************************************************
    private abstract class MonitorProcessBundle implements Runnable {
        protected Monitor _monitor;

        protected MonitorProcessBundle(Monitor monitor) {
            _monitor = monitor;
        }

        public void run() {
            for (Iterator i = _processors.iterator(); i.hasNext();) {
                MonitorProcessor processor = (MonitorProcessor) i.next();
                processWithProcessor(processor);
            }
        }

        protected abstract void processWithProcessor(MonitorProcessor processor);
    }
}
