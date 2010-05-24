package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.Attribute;

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
    /**
     * @deprecated use Attribute.RESULT_CODE instead
     */
    public static final String RESULT_CODE = Attribute.RESULT_CODE;

    /**
     * @deprecated use Attribute.START_TIME instead
     */
    protected static final String START_TIME = Attribute.START_TIME;
    /**
     * @deprecated use Attribute.END_TIME instead
     */
    protected static final String END_TIME = Attribute.END_TIME;
    /**
     * @deprecated use Attribute.LATENCY instead
     */
    protected static final String LATENCY = Attribute.LATENCY;

    /**
     * @deprecated use Attribute.FAILURE_THROWABLE instead
     */
    protected static final String FAILURE_THROWABLE = Attribute.FAILURE_THROWABLE;
    /**
     * @deprecated use Attribute.FAILED instead
     */
    protected static final String FAILED = Attribute.FAILED;
    /**
     * @deprecated use Attribute.BUSINESS_FAILURE instead
     */
    protected static final String BUSINESS_FAILURE = Attribute.BUSINESS_FAILURE;

    /**
     * Creates a new transaction monitor with the provided
     * name. The monitor is marked as failed by default. Also,
     * the start time of this transaction is noted, thereby
     * starting the stop watch.
     *
     * @param name the name of the monitr
     */
    public TransactionMonitor(String name) {
        super(name);

        startTransactionMonitor();
    }

    /**
     * Creates a new transaction monitor with the provided
     * name and monitoring level. The monitor is marked as
     * failed by default. Also, the start time of this
     * transaction is noted, thereby starting the stop watch.
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     */
    public TransactionMonitor(String name, MonitoringLevel monitoringLevel) {
        super(name, monitoringLevel);

        startTransactionMonitor();
    }

    /**
     * Create a new transaction monitor with the provided
     * name and inherited attributes. The monitor is marked
     * as failed by default. Also, the start time of this
     * transaction is noted, thereby starting the stop watch.
     *
     * @param name the name of the monitor
     * @param inheritedAttributes the collection of inherited attributes
     */
    public TransactionMonitor(String name, Map inheritedAttributes) {
        super(name, inheritedAttributes);

        startTransactionMonitor();
    }

    /**
     * Create a new transaction monitor with the provided
     * name, monitoring level and inherited attributes. The
     * monitor is marked as failed by default. Also, the
     * start time of this transaction is noted, thereby
     * starting the stop watch.
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     * @param inheritedAttributes the collection of inherited attributes
     */
    public TransactionMonitor(String name, MonitoringLevel monitoringLevel, Map inheritedAttributes) {
        super(name, monitoringLevel, inheritedAttributes);

        startTransactionMonitor();
    }

    /**
     * Create a new transaction monitor using the provided
     * class and method to generate a name. The monitor is
     * marked as failed by default. Also, the start time of
     * this transaction is noted, thereby starting the stop
     * watch.
     *
     * @param klass the class that we're monitoring
     * @param method a string containing the method name that we're monitoring
     */
    public TransactionMonitor(Class klass, String method) {
        this(formatName(klass, method));
    }

    /**
     * Create a new transaction monitor using the provided
     * class and method to generate a name, with the provided
     * monitoring level. The monitor is markedas failed by
     * default. Also, the start time of this transaction is
     * noted, thereby starting the stop watch.
     *
     * @param klass the class that we're monitoring
     * @param method a string containing the method name that we're monitoring
     * @param monitoringLevel the monitoring level
     */
    public TransactionMonitor(Class klass, String method, MonitoringLevel monitoringLevel) {
        this(formatName(klass, method), monitoringLevel);
    }

    /**
     * Create a new transaction monitor using the provided
     * class and method to generate a name, with the provided
     * inherited attributes. The monitor is markedas failed
     * by default. Also, the start time of this transaction
     * is noted, thereby starting the stop watch.
     *
     * @param klass the class that we're monitoring
     * @param method a string containing the method name that we're monitoring
     * @param inheritedAttributes the collection of inherited attributes
     */
    public TransactionMonitor(Class klass, String method, Map inheritedAttributes) {
        this(formatName(klass, method), inheritedAttributes);
    }

    /**
     * Marks this transaction as having succeeded.
     */
    public void succeeded() {
        set(Attribute.FAILED, false);
    }

    /**
     * Marks this transaction as having failed.
     */
    public void failed() {
        set(Attribute.FAILED, true);
    }

    /**
     * Marks this transaction as having failed due to the supplied Throwable.
     *
     * @param e the Throwable that caused the failure
     */
    public void failedDueTo(Throwable e) {
        set(Attribute.FAILURE_THROWABLE, e).serializable();
        failed();
    }
    
    /**
     * Stops the stop watch for this monitor. Delegates to AbstractMonitor.process().
     */
    public void done() {
        Date endTime = new Date();
        set(Attribute.END_TIME, endTime).serializable().lock();

        Date startTime = (Date) get(Attribute.START_TIME);
        set(Attribute.LATENCY, endTime.getTime() - startTime.getTime()).serializable().lock();

        process();
    }

    // format the class and method into a name
    private static String formatName(Class klass, String method) {
        return klass.getName() + "." + method;
    }

    // mark the monitor as failed and start the stop watch
    private void startTransactionMonitor() {
        set(FAILED, true).serializable();
        set(START_TIME, new Date()).serializable().lock();

        MonitoringEngine.getInstance().monitorStarted(this);
    }
}
