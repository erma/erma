package com.orbitz.monitoring.api;

/**
 * Standard monitor attribute names<br />
 * Created May 20, 2010
 * @author Greg Opaczewski
 */
public abstract class Attribute {
  /**
   * The name of this Monitor instance. This name should describe what is being monitored.
   */
  public static final String NAME = "name";
  
  /**
   * The VM id of the system that this monitor was monitoring. The MonitoringEngine sets this
   * attribute when it receives the initMonitor() callback.
   */
  public static final String VMID = "vmid";
  
  /**
   * The host name of the system that this monitor was monitoring. The MonitoringEngine sets this
   * attribute when it receives the initMonitor() callback.
   */
  public static final String HOSTNAME = "hostname";
  
  /**
   * The unqiue identifier of the thread that was being monitored. The MonitoringEngine set this
   * attribute when it receives the initMonitor() callback.
   */
  public static final String THREAD_ID = "threadId";
  
  /**
   * The time that this monitor was created. The MonitoringEngine sets this attribute when it
   * receives the initMonitor() callback.
   */
  public static final String CREATED_AT = "createdAt";
  
  /**
   * The unqiue identifier of the monitor during a given path of execution. The MonitoringEngine
   * set this attribute when it receives the initMonitor() callback.
   */
  public static final String SEQUENCE_ID = "sequenceId";
  
  /**
   * The unqiue identifier of the parent monitor during a given path of execution. The
   * MonitoringEngine set this attribute when it receives the initMonitor() callback.
   */
  public static final String PARENT_SEQUENCE_ID = "parentSequenceId";
  
  /**
   * The class of the Monitor instance.
   */
  public static final String CLASS_NAME = "className";
  
  /**
   * A code describing the state the application
   */
  public static final String RESULT_CODE = "resultCode";
  
  /**
   * {@link com.orbitz.monitoring.api.monitor.EventMonitor}: The time at which the event occurred {@link com.orbitz.monitoring.api.monitor.TransactionMonitor}: The
   * time at which the transaction started
   */
  public static final String START_TIME = "startTime";
  
  /**
   * For a {@link com.orbitz.monitoring.api.monitor.TransactionMonitor}, the time at which the transaction completed, whether
   * successfully or not
   */
  public static final String END_TIME = "endTime";
  
  /**
   * For a {@link com.orbitz.monitoring.api.monitor.TransactionMonitor}, the duration of the transaction
   */
  public static final String LATENCY = "latency";
  
  /**
   * If the {@link Monitor monitor} failed because of a {@link Throwable}, the {@link Throwable}
   * that caused the failure.
   */
  public static final String FAILURE_THROWABLE = "failureThrowable";
  
  /**
   * TODO: Define this
   */
  public static final String FAILED = "failed";
  
  /**
   * If the {@link Monitor} failed, a reason for the failure understandable by business users
   */
  public static final String BUSINESS_FAILURE = "businessFailure";
  
  /**
   * For a {@link com.orbitz.monitoring.api.monitor.ValueMonitor}, the value
   */
  public static final String VALUE = "value";
  
}
