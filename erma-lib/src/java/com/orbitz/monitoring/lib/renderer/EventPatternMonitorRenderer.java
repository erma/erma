package com.orbitz.monitoring.lib.renderer;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.Attribute;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

/**
 * EventPatternMonitorRenderer renders a Monitor recursively based on name, vmid and failed
 * attribute values.
 *
 * @author Matt O'Keefe
 */
public class EventPatternMonitorRenderer {

    private static final Logger logger = Logger.getLogger(EventPatternMonitorRenderer.class);

    private static final char delimeter = '|';

    private Set monitorsToSkip;
    private List allowedAttributes;

    public EventPatternMonitorRenderer() {
        monitorsToSkip = new HashSet();
    }

    public EventPatternMonitorRenderer(final List allowedAttributes) {
        this();
        this.allowedAttributes = allowedAttributes;
    }

    public Set getMonitorsToSkip() {
        return monitorsToSkip;
    }

    public void setMonitorsToSkip(Set monitorsToSkip) {
        this.monitorsToSkip.addAll(monitorsToSkip);
    }

    public List getAllowedAttributes() {
        return allowedAttributes;
    }

    public void setAllowedAttributes(final List allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    /**
     * Renders a Monitor recursively based on vmid, name and failed attrs.
     *
     * @param monitor
     * @return a String
     */
    public String renderMonitor(Monitor monitor) {
        StringBuffer renderBuffer = new StringBuffer();

        renderMonitorToBuffer(monitor, renderBuffer, 0);

        return renderBuffer.toString();
    }

    protected void renderMonitorToBuffer(Monitor monitor, StringBuffer buffer, int indentLevel) {
        buffer.append('\n').append(indentString(indentLevel));

        Iterator it = allowedAttributes.iterator();
        while (it.hasNext()) {
            String attributeName = (String) it.next();
            if (attributeName.equals(Attribute.FAILURE_THROWABLE)) {
                assignFailureThrowable(monitor, buffer);
            } else if (monitor.hasAttribute(attributeName)) {
                Object attributeValue = monitor.get(attributeName);
                buffer.append(attributeValue).append(delimeter);
            }
        }

        if (buffer.lastIndexOf(String.valueOf(delimeter)) == buffer.length()-1) {
            buffer.deleteCharAt(buffer.length()-1);//the last delimeter
        }
        
        if (monitor instanceof CompositeMonitor) {
            CompositeMonitor compositeMonitor = (CompositeMonitor) monitor;
            Collection childMonitors = compositeMonitor.getChildMonitors();
            if(!childMonitors.isEmpty()) {
                StringBuffer lastBuffer = null;
                int monitorCount = 1;
                for (Iterator i = childMonitors.iterator(); i.hasNext();) {
                    final Monitor childMonitor = (Monitor) i.next();
                    if(shouldRender(childMonitor)) {
                        final StringBuffer childBuffer = new StringBuffer();
                        renderMonitorToBuffer(childMonitor, childBuffer, indentLevel + 1);
                        if(lastBuffer != null && lastBuffer.toString().contentEquals(childBuffer)) {
                            monitorCount++;
                        } else {
                            if (lastBuffer != null) {
                                addChildToBuffer(buffer, lastBuffer, monitorCount);
                            }
                            lastBuffer = childBuffer;
                            monitorCount = 1;
                        }
                    }
                }
                addChildToBuffer(buffer, lastBuffer, monitorCount);
            }
        }
    }

    private void assignFailureThrowable(Monitor monitor, StringBuffer buffer) {
        if (! monitor.hasAttribute(Attribute.FAILURE_THROWABLE)) {
            return;
        }
        Object o = monitor.get(Attribute.FAILURE_THROWABLE);
        if (Throwable.class.isAssignableFrom(o.getClass())) {
            Throwable t = (Throwable) o;
            HashSet causeSet = new HashSet();
            causeSet.add(t);
            while (t.getCause() != null) {
                if(!causeSet.contains(t.getCause())) {
                    t = t.getCause();
                    causeSet.add(t);
                } else {
                    logger.warn("Unexpected cycle for failureThrowable field");
                    logger.warn(formatThrowable(t));
                    break;
                }
            }
            buffer.append(t.getClass().getName());
        } else if (LazyDynaBean.class.isAssignableFrom(o.getClass())) {
            // serialized monitors will have throwableFailure fields that are decomposed
            // into LazyDynaBean classes by the ReflectiveDecomposer, in other words this
            // block will handle Throwable's created in remote VM's
            LazyDynaBean ex = (LazyDynaBean) o;
            HashSet causeSet = new HashSet();
            causeSet.add(ex);
            while ((ex.get("cause") != null) && (LazyDynaBean.class.isAssignableFrom(ex.get("cause").getClass()))) {
                if(!causeSet.contains(ex.get("cause"))) {
                    ex = (LazyDynaBean) ex.get("cause");
                    causeSet.add(ex);
                } else {
                    logger.warn("Unexpected cycle for failureThrowable field");
                    logger.warn(formatThrowable(ex));
                    break;
                }
            }

            // the class field of any object is handled by the ClassDecomposer and
            // should therefore be of type String
            Object className = ex.get("class");
            if ((className != null) && (String.class.isAssignableFrom(className.getClass()))) {
                buffer.append(className);
            } else {
                logger.warn("Unexpected type for failureThrowable.class field");
                buffer.append("failed");
            }
        } else {
            logger.warn("Unexpected type for failureThrowable field : "+
                    o.getClass().getName());
            buffer.append("failed");
        }
    }

    protected String indentString(int indentLevel) {
        StringBuffer buf = new StringBuffer(indentLevel * 2);
        while(indentLevel > 0) {
            buf.append("  ");
            indentLevel--;
        }
        return buf.toString();
    }

    // adds the child buffer to the real buffer
    protected void addChildToBuffer(StringBuffer buffer, StringBuffer child, int count) {
        if(count > 1) {
            final int endOfFirst = child.indexOf("\n", 1);
            if (endOfFirst > 0) {
                child.insert(endOfFirst, String.valueOf(delimeter) + count + " occurences");
            } else {
                child.append(delimeter).append(count).append(" occurences");
            }
        }
        buffer.append(child);
    }

    // returns true if a monitor should be rendered, otherwise false
    protected boolean shouldRender(Monitor monitor) {
        boolean render = true;
        if(!logger.isDebugEnabled()) {
            if (monitor.hasAttribute(Attribute.NAME)) {
                String name = monitor.getAsString(Attribute.NAME);
                Iterator it = monitorsToSkip.iterator();
                while(render && it.hasNext()) {
                    String sName = (String) it.next();
                    if (name.indexOf(sName) > -1) {
                        render = false;
                    }
                }
            }
        }
        return render;
    }

    // used to prevent the stack trace from entering an infinite loop
    private String formatThrowable(Throwable t) {
        final StringBuffer sb = new StringBuffer();
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage());
        final StackTraceElement trace[] = t.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            sb.append("\n  ").append(trace[i].getClassName())
              .append(".").append(trace[i].getMethodName());
            if (trace[i].isNativeMethod()) {
                sb.append("(Native Method)");
            } else if (trace[i].getFileName() != null) {
                sb.append("(").append(trace[i].getFileName());
                if(trace[i].getLineNumber() >= 0) {
                    sb.append(":").append(trace[i].getLineNumber());
                }
                sb.append(")");
            } else {
                sb.append("(Unknown Source)");
            }
        }

        return sb.toString();
    }

    // used to format dynabeans into a standard stack trace
    private String formatThrowable(LazyDynaBean ex) {
        final StringBuffer sb = new StringBuffer();
        final Object exceptionClass = (ex.get("class") == null) ? "(Unknown Exception)" : ex.get("class");
        sb.append(exceptionClass).append(": ").append(ex.get("message"));
        final Object trace[] = (Object[]) ex.get("stackTrace");
        for (int i = 0; i < trace.length; i++) {
            final LazyDynaBean bean = (LazyDynaBean) trace[i];
            final Object nativeMethod = bean.get("nativeMethod");
            final Object lineNumber = bean.get("lineNumber");

            sb.append("\n  ").append(bean.get("className"))
              .append(".").append(bean.get("methodName"));
            /* if this doesn't work, lineNumber == -2 can be used instead */
            if (Boolean.class.isAssignableFrom(nativeMethod.getClass())
                    && ((Boolean) nativeMethod).booleanValue()) {
                sb.append("(Native Method)");
            } else if (bean.get("fileName") != null) {
                sb.append("(").append(bean.get("fileName"));
                if(Integer.class.isAssignableFrom(lineNumber.getClass())
                        && ((Integer) lineNumber).intValue() >= 0) {
                    sb.append(":").append(bean.get("lineNumber"));
                }
                sb.append(")");
            } else {
                sb.append("(Unknown Source)");
            }
        }

        return sb.toString();
    }

}
