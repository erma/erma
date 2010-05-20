package com.orbitz.monitoring.api;

public abstract class Attribute {
    /**
     * The name of this Monitor instance. This name should describe what is
     * being monitored.
     */
    public static final String NAME = "name";

    /**
     * The VM id of the system that this monitor was monitoring. The
     * MonitoringEngine sets this attribute when it receives the initMonitor()
     * callback.
     */
    public static final String VMID = "vmid";

    /**
     * The host name of the system that this monitor was monitoring. The
     * MonitoringEngine sets this attribute when it receives the initMonitor()
     * callback.
     */
    public static final String HOSTNAME = "hostname";

    /**
     * The unqiue identifier of the thread that was being monitored. The
     * MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     */
    public static final String THREAD_ID = "threadId";

    /**
     * The time that this monitor was created. The MonitoringEngine sets this
     * attribute when it receives the initMonitor() callback.
     */
    public static final String CREATED_AT = "createdAt";

    /**
     * The unqiue identifier of the monitor during a given path of execution. The
     * MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     */
    public static final String SEQUENCE_ID = "sequenceId";

    /**
     * The unqiue identifier of the parent monitor during a given path of execution.
     * The MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     */
    public static final String PARENT_SEQUENCE_ID = "parentSequenceId";

    /**
     * The class of this Monitor instance.
     */
    public static final String CLASS_NAME = "className";


    public static final String RESULT_CODE = "resultCode";

    public static final String START_TIME = "startTime";

    public static final String END_TIME = "endTime";

    public static final String LATENCY = "latency";

    public static final String FAILURE_THROWABLE = "failureThrowable";

    public static final String FAILED = "failed";

    public static final String BUSINESS_FAILURE = "businessFailure";

    public static final String VALUE = "value";

}
