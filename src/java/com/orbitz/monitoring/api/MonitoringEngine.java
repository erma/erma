package com.orbitz.monitoring.api;

import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.AttributeMap;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The engine that controls basic correlation of monitors as they are collected
 * and submitted to the engine. All monitors should call these methods at key
 * points in their lifetime so that they are processed correctly.
 *
 * @author Doug Barth
 */
public class MonitoringEngine {

    private static final Logger log = Logger.getLogger(MonitoringEngine.class);

    private static final int MAX_LEVEL_OVERRIDES = 128;

    private static MonitoringEngine instance = new MonitoringEngine();

    private boolean monitoringEnabled = true;
    private boolean running;

    private MonitorProcessorFactory processorFactory;
    private Decomposer decomposer;
    private InheritableStrategy inheritableStrategy;

    private AttributeMap globalAttributes;

    private Map monitorProcessorLevels;
    private Map monitorLevels;

    private Runnable startupRunnable;

    protected MonitoringEngine() {
        monitorProcessorLevels = new HashMap();

        monitorLevels = new TreeMap(Collections.reverseOrder());

        globalAttributes = new AttributeMap();
    }

    public static MonitoringEngine getInstance() {
        return instance;
    }

    /**
     * Starts up the monitoring engine. This method should be called before
     * using ERMA.<p>
     *
     * This call initializes the system and calls startup() on the
     * {@link MonitorProcessorFactory} supplied. Therefore, the
     * MonitorProcessorFactory to be used should have been set prior to calling
     * this method.<p>
     *
     * <b>This method is not thread-safe.</b> Client should take care to ensure
     * that multithreaded access to this method is synchronized.
     */
    public void startup() {
        log.info("MonitoringEngine starting up");

        if (processorFactory == null) {
            throw new IllegalStateException("processorFactory is null");
        }
        if (decomposer == null) {
            throw new IllegalStateException("decomposer is null");
        }

        if (inheritableStrategy == null) {
            throw new IllegalStateException("inheritableStrategy is null");
        }

        inheritableStrategy.startup();
        processorFactory.startup();

        running = true;

        if (startupRunnable != null) {
            startupRunnable.run();
        }
    }

    /**
     * Shuts down the monitoring engine. This method should be called before
     * shutting down the application to give the ERMA system a chance to cleanly
     * close all its resources.<p>
     *
     * This call disables ERMA and calls shutdown() on the
     * {@link MonitorProcessorFactory} supplied.<p>
     *
     * <b>This method is not thread-safe.</b> Client should take care to ensure
     * that multithreaded access to this method is synchronized.
     */
    public void shutdown() {
        if (running) {
            log.info("MonitoringEngine shutting down");
            globalAttributes.clear();
            monitorProcessorLevels.clear();
            monitorLevels.clear();
            running = false;
            processorFactory.shutdown();
            inheritableStrategy.shutdown();
        }
    }

    /**
     * Shuts down the MonitoringEngine if it is running. After it is shutdown,
     * the MonitoringEngine will be started up.<p>
     *
     * <b>This method is not thread-safe.</b> Client should take care to ensure
     * that multithreaded access to this method is synchronized.
     */
    public void restart() {
        if (running) {
            shutdown();
        }

        startup();
    }

    /**
     * Clears away any outstanding CompositeMonitor references held by the
     * MonitoringEngine for the current thread. This is useful to do in a
     * long-running application at some point before you create what should
     * be the first/outermost CompositeMonitor for a given thread. It reduces
     * risk of heap leaks caused by accumulating unfinished CompositeMonitor
     * instances.
     *
     * IMPORTANT NOTE:
     * This method does not "close" any monitors that are found and cleared
     * in this fashion. Only the references to them are removed from an
     * internal stack that's mapped from the current thread. Since it does
     * not close them, no MonitorProcessors will be notified of this event.
     *
     * @return count of monitor refs cleared
     */
    public int clearCurrentThread() {
        return inheritableStrategy.clearCurrentThread();
    }

    /**
     * A lifecycle method that initializes the Monitor. All monitor
     * implementations must call this methods before setting any attributes on
     * themselves.<p>
     *
     * After this method returns, the monitor will have had any implicitly
     * inherited and global attributes applied.
     *
     * @param monitor the monitor to initialize.
     */
    public void initMonitor(Monitor monitor) {
        initMonitor(monitor, true);
    }
    
    public void initMonitor(Monitor monitor, boolean includeInheritables) {
        if (!isEnabled()) {
            return;
        }

        monitor.set(Attribute.CREATED_AT, new Date()).serializable().lock();

        String threadId = Integer.toHexString(Thread.currentThread().hashCode());
        monitor.set(Attribute.THREAD_ID, threadId).serializable().lock();

        inheritGlobals(monitor);

        if (includeInheritables) {
            inheritAttributesFromAncestors(monitor);
        }
    }

    /**
     * A lifecycle method that notifies observing MonitorProcessors that a new
     * monitor has been created. All monitor implementations should call this
     * method after setting attributes known at creation on themselves.
     *
     * @param monitor the monitor that has been created
     */
    public void monitorCreated(final Monitor monitor) {
        if (!isEnabled()) {
            return;
        }

        handleMonitor(monitor, MONITOR_CREATED_CLOSURE);
    }

    private static ProcessClosure MONITOR_CREATED_CLOSURE =
            new ProcessClosure() {
                public void processWithProcessor(Monitor monitor,
                                                 MonitorProcessor processor) {
                    processor.monitorCreated(monitor);
                }
            };

    /**
     * A lifecylce method that notifies observing MonitorProcessors that a
     * monitor has been started. All monitor implementations that have a
     * start-stop concept should call this monitor at start.
     *
     * @param monitor the monitor that has started
     */
    public void monitorStarted(final Monitor monitor) {
        if (!isEnabled()) {
            return;
        }

        handleMonitor(monitor, MONITOR_STARTED_CLOSURE);
    }

    private static ProcessClosure MONITOR_STARTED_CLOSURE =
            new ProcessClosure() {
                public void processWithProcessor(Monitor monitor,
                                                 MonitorProcessor processor) {
                    processor.monitorStarted(monitor);
                }
            };

    /**
     * A lifecycle method that notifies observing MonitorProcessors that a
     * monitor is ready to be processed. All monitor implementations should call
     * as the last call of their lifecycle.
     *
     * @param monitor the monitor that should be processed
     */
    public void process(final Monitor monitor) {
        if (!isEnabled()) {
            return;
        }

        inheritableStrategy.processMonitorForCompositeMonitor(monitor);

        handleMonitor(monitor, PROCESS_CLOSURE);
    }

    private static ProcessClosure PROCESS_CLOSURE =
            new ProcessClosure() {
                public void processWithProcessor(Monitor monitor,
                                                 MonitorProcessor processor) {
                    processor.process(monitor);
                }
            };

    /**
     * Adds the supplied CompositeMonitor to the stack for this thread. If this
     * is the first CompositeMonitor on this thread, a new LinkedList is
     * created and added to a map holding all stacks by Thread.<p>
     *
     * This method should be called by all CompositeMonitor implementations
     * before they call monitorStarted().
     *
     * @param compositeMonitor the monitor to add to the stack
     */
    public void compositeMonitorStarted(CompositeMonitor compositeMonitor) {
        if (!isEnabled()) {
            return;
        }

        // this null check can probably go away if we replace the Monitor interface
        // with AbstractMonitor
        if (compositeMonitor.getLevel() == null) {
            if (log.isDebugEnabled()) {
                log.debug("skipping composite monitor with name "+
                        compositeMonitor.get(Attribute.NAME) + ", it has no defined level");
            }
            return;
        }

        inheritableStrategy.compositeMonitorStarted(compositeMonitor);
    }

    /**
     * Pops this monitor off the top of the stack. If this monitor is not on the
     * top of the stack nor found anywhere within the stack, the monitor is
     * ignored, as this is an error in instrumentation. If the monitor is
     * found within the stack, the top of the stack is repeatedly popped and
     * processed until this monitor is on the the top.<p>
     *
     * This method should be called by all CompositeMonitor implementations
     * before they call process().
     *
     * @param monitor the monitor that is completed
     */
    public void compositeMonitorCompleted(CompositeMonitor monitor) {
        if (!isEnabled()) {
            return;
        }

        inheritableStrategy.compositeMonitorCompleted(monitor);
    }

    /**
     * Obtains the first CompositeMonitor found on the per thread stack that has
     * its name attribute equal to the supplied name. This method should be used
     * in situations where stateless code is unable to hold a reference to
     * the CompositeMonitor that was originally created. Supplying the name
     * value is needed to ensure that instrumentation errors in code called by
     * users of this method does not interfere with the ability to correctly
     * obtain the original CompositeMonitor.
     *
     * @param name the value of name that our Monitor was created with.
     * @return the first CompositeMonitor with the supplied name, or null if not
     *         found
     * @throws IllegalArgumentException if name is null
     */
    public CompositeMonitor getCompositeMonitorNamed(String name) throws IllegalArgumentException {
        return inheritableStrategy.getCompositeMonitorNamed(name);
    }

    /**
     * Returns the current inheritable attributes for this thread.
     *
     * @return the inheritable attributes that would be applied to a monitor
     *         if it were made right now, or an empty Map if there are none
     */
    public Map getInheritableAttributes() {
        return inheritableStrategy.getInheritableAttributes();
    }

    /**
     * Takes the supplied attributes and returns an equivalent set that are
     * ready to be serialized.  Will only return those attributes that are
     * marked as serializable.
     *
     * @param attributeHolders the attributes to prepare for serialization
     * @return an equivalent set of attributes that can be serialized
     */
    public Map makeAttributeHoldersSerializable(Map attributeHolders) {

        if (!isEnabled()) {
            return new HashMap();
        }

        Map renderedAttributes = new HashMap(attributeHolders.size());

        for (Iterator i = attributeHolders.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            AttributeHolder holder = (AttributeHolder)entry.getValue();
            if (holder.isSerializable()) {
                renderedAttributes.put(key, decomposer.decompose(holder));
            }
        }

        return renderedAttributes;
    }

    /**
     * Sets a global attribute with an Object value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, Object value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a short value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, short value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with an int value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, int value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a long value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, long value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a float value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, float value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a double value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, double value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a char value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, char value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a byte value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, byte value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a boolean value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, boolean value) {
        globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a map of values. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param attributes the map of attributes to set
     */
    public void setGlobalAttributes(Map attributes) {
        globalAttributes.setAll(attributes);
    }

    public AttributeHolder setGlobal(String key, String value) {
        return globalAttributes.set(key, value).serializable();
    }

    /**
     * Gets enabled state of MonitoringEngine.  No monitors are processed until
     * this returns true.
     *
     * @return true if monitoringEnabled (default) and when running (between startup()
     *  and shutdown() in lifecycle) 
     */
    public boolean isEnabled() {
        return monitoringEnabled && running;
    }

    /**
     * Sets a Runnable to be executed on startup of the MonitoringEngine.
     * @param startupRunnable instance of a Runnable
     */
    public void setStartupRunnable(Runnable startupRunnable) {
        this.startupRunnable = startupRunnable;
    }

    /**
     * This method encapsulates the logic of looping over all applicable
     * processors and applying the supplied closure on each processor.
     *
     * <p>This method catchs Throwable to ensure that the client code is not
     * affected by errors in the monitoring framework. Throwables thrown from
     * processors are also caught separately to ensure that one failing
     * processor does not affect other working processors.
     *
     * @param monitor the monitor to handle
     * @param closure the work we should perform across each processor
     */
    private void handleMonitor(Monitor monitor, ProcessClosure closure) {
        try {
            MonitorProcessor[] processors = processorFactory.getProcessorsForMonitor(monitor);

            if (log.isDebugEnabled()) {
                log.debug(monitor + " will be processed by "
                        + Arrays.asList(processors));
            }

            for (int i = 0; i < processors.length; i++) {
                try {
                    closure.processWithProcessor(monitor, processors[i]);
                } catch (Throwable t) {
                    log.warn("Throwable caught while processing " + monitor +
                            "; application is unaffected: ", t);
                }
            }
        } catch (Throwable t) {
            log.warn("Throwable caught while processing " + monitor
                    + "; application is unaffected: ", t);
        }
    }

    private void inheritGlobals(Monitor monitor) {
        for (Iterator it = globalAttributes.getAllAttributeHolders().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            AttributeHolder holder = (AttributeHolder)entry.getValue();

            Object value = holder.getValue();
            AttributeHolder attribute = monitor.set(key, value);

            if (holder.isSerializable()) attribute.serializable();
            if (holder.isLocked()) attribute.lock();
        }
    }

    private void inheritAttributesFromAncestors(Monitor monitor) {
        // Inherit from parent if not set.
        Map attrs = getInheritableAttributes();

        for (Iterator it = attrs.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            AttributeHolder parentAttribute = (AttributeHolder) entry.getValue();

            if (!monitor.hasAttribute(key)) {
                Object value = parentAttribute.getValue();
                AttributeHolder childAttribute = monitor.set(key, value);
                
                if (parentAttribute.isSerializable()) childAttribute.serializable();
                if (parentAttribute.isLocked()) childAttribute.lock();
            }
        }
    }

    public MonitorProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    public void setProcessorFactory(MonitorProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    public Decomposer getDecomposer() {
        return decomposer;
    }

    public void setDecomposer(Decomposer decomposer) {
        this.decomposer = decomposer;
    }

    public InheritableStrategy getInheritableStrategy() {
        return inheritableStrategy;
    }

    public void setInheritableStrategy(InheritableStrategy inheritableStrategy) {
        this.inheritableStrategy = inheritableStrategy;
    }

    public void setMonitoringEnabled(boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public void addProcessorLevel(String name, MonitoringLevel level) {
        if (name == null) {
            throw new NullPointerException("null processor name");
        }

        monitorProcessorLevels.put(name, level);
    }

    public String getOverrideProcessorLevelsListing() {
        return monitorProcessorLevels.toString();
    }

    public void addMonitorLevel(String nameStartsWith, MonitoringLevel level) {
        if (nameStartsWith == null) {
            throw new NullPointerException("null monitor name");
        }
        if (monitorLevels.size() >= MAX_LEVEL_OVERRIDES) {
            throw new RuntimeException("Attempt to exceed max cache size for override levels");
        }

        monitorLevels.put(nameStartsWith, level);
    }

    public String getOverrideMonitorLevelsListing() {
        return monitorLevels.toString();
    }

    /**
     * Given the name of a MonitorProcessor, return the MonitoringLevel
     * that should be used for that MonitorProcessor.  If no level has
     * been specified then return null.
     * @param name the name of the MonitorProcessor to retrieve the level for.
     * @return a MonitoringLevel appropriate for the MonitorProcessor, or null
     * if one does not apply.
     */
    public MonitoringLevel getProcessorLevel(String name) {
        return (MonitoringLevel) monitorProcessorLevels.get(name);
    }

    /**
     * Given a monitor, look at its name and see if there has been
     * an updated level set during runtime.  The updated level can
     * apply for a specific monitor "com.orbitz.foo.bar" or any
     * package "com.orbitz.foo"
     *
     * @param monitor the monitor to check for an updated level
     * @return the appropiate level for this monitor, if a new
     * level has been set at runtime that will be returned, if
     * not the monitor's level set at construction time will be
     * returned.
     */
    public MonitoringLevel getOverrideLevelForMonitor(Monitor monitor) {
        String name = monitor.getAsString(Attribute.NAME);

        Set keys = monitorLevels.keySet();
        Iterator itr = keys.iterator();

        String keyToUse = null;
        while (itr.hasNext()) {
            String key = (String) itr.next();

            if (name.startsWith(key)) {
                keyToUse = key;
                break;
            }
        }

        return (keyToUse != null ? (MonitoringLevel) monitorLevels.get(keyToUse) : null);
    }

    public void setInheritable(CompositeMonitor compositeMonitor, String key, AttributeHolder original) {
        if(isEnabled() && running) {
            inheritableStrategy.setInheritable(compositeMonitor,  key, original);
        }
    }

    private static interface ProcessClosure {
        public void processWithProcessor(Monitor monitor, MonitorProcessor processor);
    }

}
