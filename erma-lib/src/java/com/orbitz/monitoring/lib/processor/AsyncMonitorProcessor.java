package com.orbitz.monitoring.lib.processor;

import com.google.common.collect.Lists;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorAttachable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Processes monitors on a separate thread. This is recommended for use when decoupling the
 * processing of monitors from the gathering of the data is allowed.
 * @author Doug Barth
 */
public final class AsyncMonitorProcessor extends MonitorProcessorAdapter implements MonitorProcessorAttachable {
  private String _name;
  private List<MonitorProcessor> _processors;
  private ExecutorService _monitorProcessingExecutor;
  
  /**
   * Creates a new async monitor processor with an empty list of {@link MonitorProcessor processors}
   */
  public AsyncMonitorProcessor() {
    _processors = new ArrayList<MonitorProcessor>();
  }
  
  /**
   * Creates a new async monitor processor with a list of processors
   * @since 3.5
   * @param processors
   */
  public AsyncMonitorProcessor(MonitorProcessor... processors) {
    this();
    if (processors != null) {
      _processors = Lists.newArrayList(processors);
    }
  }
  
  public void startup() {
    _monitorProcessingExecutor = Executors.newSingleThreadExecutor();
    for (MonitorProcessor processor : _processors) {
      processor.startup();
    }
  }
  
  public void shutdown() {
    flushEvents();
    _monitorProcessingExecutor.shutdownNow();
    for (MonitorProcessor processor : _processors) {
      processor.shutdown();
    }
  }
  
  public void monitorCreated(final Monitor monitor) {
    _monitorProcessingExecutor.execute(new MonitorProcessBundle(monitor.getSerializableMomento()) {
      @Override
      protected void processWithProcessor(final MonitorProcessor processor) {
        processor.monitorCreated(_monitor);
      }
    });
  }
  
  public void monitorStarted(final Monitor monitor) {
    _monitorProcessingExecutor.execute(new MonitorProcessBundle(monitor.getSerializableMomento()) {
      @Override
      protected void processWithProcessor(final MonitorProcessor processor) {
        processor.monitorStarted(_monitor);
      }
    });
  }
  
  public void process(final Monitor monitor) {
    _monitorProcessingExecutor.execute(new MonitorProcessBundle(monitor.getSerializableMomento()) {
      @Override
      protected void processWithProcessor(final MonitorProcessor processor) {
        processor.process(_monitor);
      }
    });
  }
  
  /**
   * Replaces the {@link java.util.concurrent.Executor executor} that is processing {@link Monitor monitors}, then shuts
   * down the old monitor and waits up to 100ms for it to finish shutting down.
   */
  public void flushEvents() {
    final ExecutorService oldMonitorProcessingExecutor = _monitorProcessingExecutor;
    _monitorProcessingExecutor = Executors.newSingleThreadExecutor();
    oldMonitorProcessingExecutor.shutdown();
    while (!oldMonitorProcessingExecutor.isTerminated()) {
      try {
        oldMonitorProcessingExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
  }
  
  /**
   * @see MonitorProcessorAttachable#addMonitorProcessor(com.orbitz.monitoring.api.MonitorProcessor)
   */
  public void addMonitorProcessor(final MonitorProcessor processor) {
    _processors.add(processor);
  }
  
  /**
   * @see com.orbitz.monitoring.api.MonitorProcessorAttachable#getMonitorProcessors()
   */
  public List<MonitorProcessor> getMonitorProcessors() {
    return _processors;
  }
  
  /**
   * @see com.orbitz.monitoring.api.MonitorProcessor#getName()
   */
  public String getName() {
    return _name;
  }
  
  /**
   * Sets the name of this processor
   * @param name the name to set
   */
  public void setName(final String name) {
    _name = name;
  }
  
  private abstract class MonitorProcessBundle implements Runnable {
    protected Monitor _monitor;
    
    protected MonitorProcessBundle(final Monitor monitor) {
      _monitor = monitor;
    }
    
    public void run() {
      for (MonitorProcessor processor : _processors) {
        processWithProcessor(processor);
      }
    }
    
    protected abstract void processWithProcessor(MonitorProcessor processor);
  }
}
