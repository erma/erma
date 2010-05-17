package com.orbitz.monitoring.api.engine;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 */
public class StackBasedInheritableStrategy implements InheritableStrategy {

    private static final Logger log = Logger.getLogger(StackBasedInheritableStrategy.class);

    private static final String DEFAULT_PARENT_SEQUENCE_ID = "m";

    private final ConcurrentMap threadBasedMap = new ConcurrentHashMap();
    private AtomicReference eventPatternLevel = new AtomicReference(MonitoringLevel.INFO);

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
     * This method should be called by all CompositeMonitor implementations
     * before they call process().
     *
     * @param monitor the monitor that is completed
     */
    public void compositeMonitorCompleted(CompositeMonitor monitor) {
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
                MonitoringEngine.getInstance().process(missedMonitor);
            }

            stack.removeLast();
        }
    }

    /**
     * This method should be called by all CompositeMonitor implementations
     * before they call monitorStarted().
     *
     * @param compositeMonitor the composite monitor
     */
    public void compositeMonitorStarted(CompositeMonitor compositeMonitor) {
        if (getEventPatternLevel().hasHigherPriorityThan(compositeMonitor.getLevel())) {
            if (log.isDebugEnabled()) {
                log.debug("skipping " + compositeMonitor.getAsString(Attribute.NAME));
            }
            return;
        }

        LinkedList stack = getStack();

        if (stack == null) {
            stack = new LinkedList();
            threadBasedMap.put(Thread.currentThread(), stack);
        }

        stack.addLast(new StackFrame(compositeMonitor));
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

    public void processMonitorForCompositeMonitor(Monitor monitor) {
        LinkedList stack = getStack();

        if (stack != null) {
            if (! stack.isEmpty()) {
                StackFrame stackFrame = (StackFrame) stack.getLast();
                CompositeMonitor parentMonitor = stackFrame.getCompositeMonitor();

                // only add this monitor being processed to a parent if it is enabled
                // by its monitoring level
                MonitoringLevel monitorLevel = monitor.getLevel();

                if ((monitorLevel != null) && (monitorLevel.hasHigherOrEqualPriorityThan(getEventPatternLevel()))) {
                    parentMonitor.addChildMonitor(monitor);
                }
            } else {
                threadBasedMap.remove(Thread.currentThread());
            }
        }
    }

    public void setInheritable(CompositeMonitor monitor, String key, AttributeHolder origional) {
        // no-op
    }

    public void shutdown() {
        threadBasedMap.clear();
    }

    public void startup() {
        threadBasedMap.clear();
    }

    public MonitoringLevel getEventPatternLevel() {
        return (MonitoringLevel) eventPatternLevel.get();
    }

    public void setEventPatternLevel(MonitoringLevel eventPatternLevel) {
        this.eventPatternLevel.set(eventPatternLevel);
    }

    private LinkedList getStack() {
        return (LinkedList) threadBasedMap.get(Thread.currentThread());
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
