package com.orbitz.monitoring.api;

/**
 * Created by IntelliJ IDEA.
 * User: smullins
 * Date: Feb 21, 2009
 * Time: 9:20:46 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Attribute {
    /**
     * The name of this Monitor instance. This name should describe what is
     * being monitored.
     */
    static final String NAME = "name";

    /**
     * The VM id of the system that this monitor was monitoring. The
     * MonitoringEngine sets this attribute when it receives the initMonitor()
     * callback.
     */
    static final String VMID = "vmid";

    /**
     * The host name of the system that this monitor was monitoring. The
     * MonitoringEngine sets this attribute when it receives the initMonitor()
     * callback.
     */
    static final String HOSTNAME = "hostname";

    /**
     * The unqiue identifier of the thread that was being monitored. The
     * MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     */
    static final String THREAD_ID = "threadId";

    /**
     * The time that this monitor was created. The MonitoringEngine sets this
     * attribute when it receives the initMonitor() callback.
     */
    static final String CREATED_AT = "createdAt";

    /**
     * The unqiue identifier of the monitor during a given path of execution. The
     * MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     */
    static final String SEQUENCE_ID = "sequenceId";

    /**
     * The unqiue identifier of the parent monitor during a given path of execution.
     * The MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     */
    static final String PARENT_SEQUENCE_ID = "parentSequenceId";

    /**
     * The class of this Monitor instance.
     */
    static final String CLASS_NAME = "className";


    static final String RESULT_CODE = "resultCode";

    static final String START_TIME = "startTime";

    static final String END_TIME = "endTime";

    static final String LATENCY = "latency";

    static final String FAILURE_THROWABLE = "failureThrowable";

    static final String FAILED = "failed";

    static final String BUSINESS_FAILURE = "businessFailure";

    static final String TRANSACTION_MONITOR = "TransactionMonitor";
}
