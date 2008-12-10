package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;

import java.util.Map;
import java.util.Date;

/**
 * A monitor for transactions. Transactions implicitly have durations. In order
 * to prevent instrumentation errors from showing failing transactions as
 * successful, TransactionMonitors assume that a transaction failed unless
 * success is called.
 *
 * @author Doug Barth
 */
public class TransactionMonitor extends AbstractCompositeMonitor {

    public static final String RESULT_CODE = "resultCode";

    protected static final String START_TIME = "startTime";
    protected static final String END_TIME = "endTime";
    protected static final String LATENCY = "latency";

    protected static final String FAILURE_THROWABLE = "failureThrowable";
    protected static final String FAILED = "failed";
    protected static final String BUSINESS_FAILURE = "businessFailure";

    protected static final String TRANSACTION_MONITOR = "TransactionMonitor";

    /**
     * Creates a new TransactionMonitor with the given name. The monitor is
     * marked as failed by default. Also, the start time of this transaction is
     * noted, thereby starting the stop watch.
     *
     * @param name the name of this transaction
     */
    public TransactionMonitor(String name) {
        super(name);

        startTransactionMonitor();
    }

    /**
     * Creates a new TransactionMonitor with the given name and level.
     *
     * @param name the name of this transaction
     * @param monitoringLevel monitoring level
     */
    public TransactionMonitor(String name, MonitoringLevel monitoringLevel) {
        super(name, monitoringLevel);

        startTransactionMonitor();
    }

    /**
     * Creates a new TransactionMonitor with the given name and default
     * attributes. The transaction is marked failed by default. The start time
     * is also noted.
     *
     * @param name the name of this transaction
     * @param defaultAttributes attributes that should be set on this
     *        monitor immediately
     */
    public TransactionMonitor(String name, Map defaultAttributes) {
        super(name, defaultAttributes);

        startTransactionMonitor();
    }

    /**
     * Creates a new TransactionMonitor with the given name, level and default
     * attributes. The transaction is marked failed by default. The start time
     * is also noted.
     *
     * @param name the name of this transaction
     * @param monitoringLevel monitoring level
     * @param defaultAttributes attributes that should be set on this
     *        monitor immediately
     */
    public TransactionMonitor(String name, MonitoringLevel monitoringLevel, Map defaultAttributes) {
        super(name, monitoringLevel, defaultAttributes);

        startTransactionMonitor();
    }

    /**
     * Creates a new TransactionMonitor with a name obtained by concatenating
     * the class name and method string together.
     *
     * @param klass the class that we're monitoring
     * @param method a string containing the method name that we're monitoring
     */
    public TransactionMonitor(Class klass, String method) {
        this(formatName(klass, method));
    }

    /**
     * Creates a new TransactionMonitor with a name obtained by concatenating
     * the class name and method string together, with the given monitoring level
     *
     * @param klass the class that we're monitoring
     * @param method a string containing the method name that we're monitoring
     * @param level monitoring level
     */
    public TransactionMonitor(Class klass, String method, MonitoringLevel level) {
        this(formatName(klass, method), level);
    }

    /**
     * Creates a new TransactionMonitor with default attributes and a name
     * composed of the class name and method name.
     *
     * @param klass the class that we're monitoring
     * @param method a string containing the method name that we're monitoring
     * @param defaultAttributes the default attributes for this monitor
     */
    public TransactionMonitor(Class klass, String method, Map defaultAttributes) {
        this(formatName(klass, method), defaultAttributes);
    }

    // ** PUBLIC METHODS ******************************************************
    /**
     * Marks this transaction as having succeeded.
     */
    public void succeeded() {
        set(FAILED, false);
    }

    /**
     * Marks this transaction as having failed.
     */
    public void failed() {
        set(FAILED, true);
    }

    /**
     * Marks this transaction as having failed due to the supplied Throwable.
     *
     * @param e the Throwable that caused the failure
     */
    public void failedDueTo(Throwable e) {
        set(FAILURE_THROWABLE, e).serializable();
        failed();
    }

    // ** PROTECTED METHODS ***************************************************
    
    /**
     * Stops the stop watch for this monitor and submits it for processing.
     */
    public void done() {
        //if (MonitoringEngine.getInstance().isEnabled()) assert hasAttribute(CREATED_AT);
        //if (MonitoringEngine.getInstance().isEnabled()) assert hasAttribute(FAILED);
        //if (MonitoringEngine.getInstance().isEnabled()) assert hasAttribute(START_TIME);

        Date endTime = new Date();
        set(END_TIME, endTime).serializable().lock();

        Date startTime = (Date) get(START_TIME);
        set(LATENCY, endTime.getTime() - startTime.getTime()).serializable().lock();

        process();
    }

    // ** PRIVATE METHODS *****************************************************
    private static String formatName(Class klass, String method) {
        return klass.getName() + "." + method;
    }

    private void startTransactionMonitor() {
        //if (MonitoringEngine.getInstance().isEnabled()) assert hasAttribute(CREATED_AT);

        set(FAILED, true).serializable();
        set(START_TIME, new Date()).serializable().lock();

        MonitoringEngine.getInstance().monitorStarted(this);
    }
}
