package com.orbitz.monitoring.api;

import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.AttributeMap;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentMap;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
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

    private static final String DEFAULT_PARENT_SEQUENCE_ID = "m";

    private static final int MAX_LEVEL_OVERRIDES = 128;

    private static MonitoringEngine _instance = new MonitoringEngine();

    private boolean _monitoringEnabled = true;
    private boolean _running;
    private MonitoringLevel _eventPatternMonitoringLevel = MonitoringLevel.INFO;

    private final ConcurrentMap _syncedThreadToStack;
    private MonitorProcessorFactory _processorFactory;
    private Decomposer _decomposer;

    private AttributeMap _globalAttributes;

    private Map _monitorProcessorLevels;
    private Map _monitorLevels;
    private AtomicReference _atomicEPMLevel;

    private Runnable _startupRunnable;

    protected MonitoringEngine() {
        _syncedThreadToStack = new ConcurrentHashMap();

        _monitorProcessorLevels = new HashMap();

        _monitorLevels = new TreeMap(Collections.reverseOrder());
        
        _atomicEPMLevel = new AtomicReference(_eventPatternMonitoringLevel);

        _globalAttributes = new AttributeMap();
    }

    public static MonitoringEngine getInstance() {
        return _instance;
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

        if (_processorFactory == null) {
            throw new IllegalStateException("processorFactory is null");
        }
        if (_decomposer == null) {
            throw new IllegalStateException("decomposer is null");
        }

        _syncedThreadToStack.clear();
        _processorFactory.startup();

        _running = true;

        if (_startupRunnable != null) {
            _startupRunnable.run();
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
        if (_running) {
            log.info("MonitoringEngine shutting down");
            _globalAttributes.clear();
            _monitorProcessorLevels.clear();
            _monitorLevels.clear();
            _running = false;
            _processorFactory.shutdown();
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
        if (_running) {
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
        LinkedList stack = getStack();
        int count = 0;
        if (stack != null) {
            count = stack.size();
            if (count > 0) {
                StringBuffer monitorNames = new StringBuffer();
                for (Iterator i = stack.iterator(); i.hasNext();) {
                    StackFrame stackFrame = (StackFrame) i.next();
                    Monitor m = stackFrame.getCompositeMonitor();
                    String s = (String) m.get(Attribute.NAME);
                    if (monitorNames.length() > 0) {
                        monitorNames.append(", ");
                    }
                    monitorNames.append(s);
                }
                log.warn("clearing old CompositeMonitor refs for current thread; "+count+" found; names: "+monitorNames);
                stack.clear();
            }
        }
        return count;
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
        if (!isEnabled()) {
            return;
        }

        monitor.set(Attribute.CREATED_AT, new Date()).serializable().lock();

        String threadId = Integer.toHexString(Thread.currentThread().hashCode());
        monitor.set(Attribute.THREAD_ID, threadId).serializable().lock();

        inheritAttributesFromParent(monitor);
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

        processMonitorForCompositeMonitor(monitor);

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
        MonitoringLevel compositeMonitorLevel = compositeMonitor.getLevel();

        // this null check can probably go away if we replace the Monitor interface
        // with AbstractMonitor
        if (compositeMonitorLevel == null) {
            if (log.isDebugEnabled()) {
                log.debug("skipping composite monitor with name "+
                        compositeMonitor.get(Attribute.NAME) + ", it has no defined level");
            }
            return;
        }

        MonitoringLevel epmLevel = (MonitoringLevel) _atomicEPMLevel.get();
        if (!isEnabled() || epmLevel.hasHigherPriorityThan(compositeMonitorLevel)) {
            if (log.isDebugEnabled()) {
                log.debug("skipping " + compositeMonitor.getAsString(Attribute.NAME));
            }
            return;
        }

        LinkedList stack = getStack();

        if (stack == null) {
            stack = new LinkedList();
            _syncedThreadToStack.put(Thread.currentThread(), stack);
        }

        stack.addLast(new StackFrame(compositeMonitor));
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

        LinkedList stack = getStack();

        if (stack != null) {
            StackFrame target = new StackFrame(monitor);
            if (!stack.getLast().equals(target) && !stack.contains(target)) {
                // This monitor is being double processed on accident.
                // Ignore it.
                return;
            }

            while (!stack.getLast().equals(target)) {
                // A child monitor was not processed, process them now.
                StackFrame stackFrame = (StackFrame) stack.removeLast();
                CompositeMonitor missedMonitor = stackFrame.getCompositeMonitor();
                String name = (String) missedMonitor.get(Attribute.NAME);
                log.warn("unfinished child monitor \""+name+"\" found so will process now and remove; app is fine");
                process(missedMonitor);
            }

            stack.removeLast();
        }
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
    public CompositeMonitor getCompositeMonitorNamed(String name)
            throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        CompositeMonitor monitorToReturn = null;

        LinkedList stack = getStack();

        if (stack != null) {
            int size = stack.size();
            ListIterator i = stack.listIterator(size);

            while (i.hasPrevious()) {
                StackFrame stackFrame = (StackFrame) i.previous();
                CompositeMonitor monitor = stackFrame.getCompositeMonitor();

                if (name.equals(monitor.get(Attribute.NAME))) {
                    monitorToReturn = monitor;
                    break;
                }
            }
        }

        return monitorToReturn;
    }

    /**
     * Returns the current inheritable attributes for this thread.
     *
     * @return the inheritable attributes that would be applied to a monitor
     *         if it were made right now, or an empty Map if there are none
     */
    public Map getInheritableAttributes() {
        Map inheritable = new HashMap();

        String parentSequenceId = DEFAULT_PARENT_SEQUENCE_ID;

        LinkedList stack = getStack();

        if (stack != null && !stack.isEmpty()) {
            Iterator i = stack.iterator();
            while (i.hasNext()) {
                StackFrame stackFrame = (StackFrame) i.next();
                CompositeMonitor monitor = stackFrame.getCompositeMonitor();
                inheritable.putAll(monitor.getInheritableAttributeHolders());
            }
            StackFrame stackFrame = (StackFrame) stack.getLast();
            CompositeMonitor parent = stackFrame.getCompositeMonitor();

            parentSequenceId = parent.getAsString(Attribute.SEQUENCE_ID);

            inheritable.put(Attribute.PARENT_SEQUENCE_ID,
                    new CompositeAttributeHolder(parentSequenceId, true).serializable().lock());

        }

        String sequenceId = parentSequenceId;
        if (stack != null && !stack.isEmpty()) {
            StackFrame stackFrame = (StackFrame) stack.getLast();
            AtomicInteger counter = stackFrame.getCounter();
            sequenceId += "_" + counter.getAndIncrement();
        }

        inheritable.put(Attribute.SEQUENCE_ID, new CompositeAttributeHolder(sequenceId, true).serializable().lock());

        return inheritable;
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
                renderedAttributes.put(key, _decomposer.decompose(holder));
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
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a short value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, short value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with an int value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, int value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a long value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, long value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a float value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, float value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a double value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, double value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a char value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, char value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a byte value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, byte value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a boolean value. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param key the attribute to set
     * @param value the value of that attribute
     */
    public void setGlobalAttribute(String key, boolean value) {
        _globalAttributes.set(key, value);
    }

    /**
     * Sets a global attribute with a map of values. Global attributes will be
     * set on all monitors during the initMonitor() call.
     *
     * @param attributes the map of attributes to set
     */
    public void setGlobalAttributes(Map attributes) {
        _globalAttributes.setAll(attributes);
    }

    public AttributeHolder setGlobal(String key, String value) {
        return _globalAttributes.set(key, value).serializable();
    }

    /**
     * Gets enabled state of MonitoringEngine.  No monitors are processed until
     * this returns true.
     *
     * @return true if monitoringEnabled (default) and when running (between startup()
     *  and shutdown() in lifecycle) 
     */
    public boolean isEnabled() {
        return _monitoringEnabled && _running;
    }

    /**
     * Sets a Runnable to be executed on startup of the MonitoringEngine.
     * @param startupRunnable instance of a Runnable
     */
    public void setStartupRunnable(Runnable startupRunnable) {
        _startupRunnable = startupRunnable;
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
    private void handleMonitor(Monitor monitor,
                               ProcessClosure closure) {
        try {

            MonitorProcessor[] processors = _processorFactory.getProcessorsForMonitor(monitor);

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

    /**
     * Set global attributes on the monitor.
     *
     * @param monitor the monitor
     */
    public void initGlobalAttributes(Monitor monitor) {
        for (Iterator it = _globalAttributes.getAllAttributeHolders().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            AttributeHolder holder = (AttributeHolder)entry.getValue();

            Object value = holder.getValue();
            AttributeHolder attribute = monitor.set(key, value);

            if (holder.isSerializable()) attribute.serializable();
            if (holder.isLocked()) attribute.lock();
        }
    }

    private void inheritAttributesFromParent(Monitor monitor) {
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

    private void processMonitorForCompositeMonitor(Monitor monitor) {
        LinkedList stack = getStack();

        if (stack != null) {
            if (! stack.isEmpty()) {
                StackFrame stackFrame = (StackFrame) stack.getLast();
                CompositeMonitor parentMonitor = stackFrame.getCompositeMonitor();

                // only add this monitor being processed to a parent if it is enabled
                // by its monitoring level
                MonitoringLevel monitorLevel = monitor.getLevel();
                MonitoringLevel epmLevel = (MonitoringLevel) _atomicEPMLevel.get();
                
                if ((monitorLevel != null) && (monitorLevel.hasHigherOrEqualPriorityThan(epmLevel))) {             
                    parentMonitor.addChildMonitor(monitor);
                }
            } else {
                _syncedThreadToStack.remove(Thread.currentThread());
            }
        }
    }

    private LinkedList getStack() {
        return (LinkedList) _syncedThreadToStack.get(Thread.currentThread());
    }

    public MonitorProcessorFactory getProcessorFactory() {
        return _processorFactory;
    }

    public void setProcessorFactory(MonitorProcessorFactory processorFactory) {
        _processorFactory = processorFactory;
    }

    public Decomposer getDecomposer() {
        return _decomposer;
    }

    public void setDecomposer(Decomposer decomposer) {
        _decomposer = decomposer;
    }

    public void setMonitoringEnabled(boolean monitoringEnabled) {
        _monitoringEnabled = monitoringEnabled;
    }

    public MonitoringLevel getEventPatternMonitoringLevel() {
        return (MonitoringLevel) _atomicEPMLevel.get();
    }

    public void setEventPatternMonitoringLevel(MonitoringLevel eventPatternMonitoringLevel) {
        _atomicEPMLevel.set(eventPatternMonitoringLevel);
    }

    public void addProcessorLevel(String name, MonitoringLevel level) {
        if (name == null) {
            throw new NullPointerException("null processor name");
        }

        _monitorProcessorLevels.put(name, level);
    }

    public String getOverrideProcessorLevelsListing() {
        return _monitorProcessorLevels.toString();
    }

    public void addMonitorLevel(String nameStartsWith, MonitoringLevel level) {
        if (nameStartsWith == null) {
            throw new NullPointerException("null monitor name");
        }
        if (_monitorLevels.size() >= MAX_LEVEL_OVERRIDES) {
            throw new RuntimeException("Attempt to exceed max cache size for override levels");
        }

        _monitorLevels.put(nameStartsWith, level);
    }

    public String getOverrideMonitorLevelsListing() {
        return _monitorLevels.toString();
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
        return (MonitoringLevel)_monitorProcessorLevels.get(name);
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

        Set keys = _monitorLevels.keySet();
        Iterator itr = keys.iterator();

        String keyToUse = null;
        while (itr.hasNext()) {
            String key = (String) itr.next();

            if (name.startsWith(key)) {
                keyToUse = key;
                break;
            }
        }

        return (keyToUse != null ? (MonitoringLevel)_monitorLevels.get(keyToUse) : null);
    }

    private static interface ProcessClosure {
        public void processWithProcessor(Monitor monitor,
                                         MonitorProcessor processor);
    }

    /**
     * Private class used in synced stacks.
     */
    private class StackFrame {

        private final CompositeMonitor _monitor;
        private final AtomicInteger _counter;

        public StackFrame(CompositeMonitor monitor) {
            super();
            _monitor = monitor;
            _counter = new AtomicInteger(0);
        }

        public CompositeMonitor getCompositeMonitor() {
            return _monitor;
        }

        public AtomicInteger getCounter() {
            return _counter;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final StackFrame other = (StackFrame) o;

            return (_monitor == null ? other._monitor == null : _monitor == other._monitor);
        }

        public int hashCode() {
            return  (_monitor == null ? 0 : _monitor.hashCode());
        }

    }
}
