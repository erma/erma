package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.renderer.XmlMonitorRenderer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Annotates monitors with an xml event pattern built from the given monitor tree.
 * This processor is also exported as a JMX mbean, exposing runtime controls to
 * enable/disable the processor and to change the set of xml attributes rendered.
 *
 * <p/>
 * @author Operations Architecture
 *
 * @@org.springframework.jmx.export.metadata.ManagedResource
 * (description="Runtime controls for the EventPatternMonitorProcessor.")
 */
public class EventPatternMonitorProcessor extends MonitorProcessorAdapter {
    // ** STATIC/FINAL DATA ***************************************************
    protected Logger logger = Logger.getLogger(EventPatternMonitorProcessor.class);

    private static final List DEFAULT_ATTRIBUTES = new ArrayList();
    static {
        DEFAULT_ATTRIBUTES.add("name");
        DEFAULT_ATTRIBUTES.add("vmid");
        DEFAULT_ATTRIBUTES.add("failed");
        DEFAULT_ATTRIBUTES.add("failureThrowable");
        DEFAULT_ATTRIBUTES.add("latency");
    }

    private AtomicReference<List> allowedAttributes =
            new AtomicReference<List>(DEFAULT_ATTRIBUTES);

    private AtomicInteger maxPatternSize = new AtomicInteger(50000);

    private boolean enabled = true;

    /**
     * Render monitors to xml and annotate the given monitor with the xml string.
     *
     * @param monitor Monitor tree for completed event pattern
     */
    public void process(Monitor monitor) {
        if (!enabled) return;

        // render monitor to xml
        XmlMonitorRenderer renderer = null;

        renderer = new XmlMonitorRenderer(allowedAttributes.get());
        renderer.setMaxCharacters(maxPatternSize.get());
        String xml = renderer.renderMonitor(monitor);

        if (logger.isDebugEnabled()) {
            logger.debug("Event pattern XML:\n" + xml);
        }

        // annotate monitor with xml
        monitor.set("eventPatternXml", xml);
    }

    public void setAllowedAttributes(List allowedAttributes) {
        this.allowedAttributes.set(allowedAttributes);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="get the maximum event pattern xml size")
     *
     * @return the maximum pattern size
     */
    public int getMaxPatternSize() {
        return maxPatternSize.get();
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="set the maximum event pattern xml size")
     *
     * @param maxPatternSize the maximum pattern size
     */
    public void setMaxPatternSize(int maxPatternSize) {
        this.maxPatternSize.set(maxPatternSize);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="true if this feature is enabled")
     *
     * @return boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="set to true to enable this feature")
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedOperation (description="list attributes enabled in ")
     */
    public String listXmlAttributes() {
        return allowedAttributes.get().toString();
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedOperation (description="add a new attribute to the generated xml")
     *
     * @param attributeName Name of attribute rendered from the ERMA monitor to the pattern xml.
     */
    public synchronized void addXmlAttribute(String attributeName) {
        List newList = new ArrayList(allowedAttributes.get());
        newList.add(attributeName);
        allowedAttributes.set(newList);
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedOperation (description="remove an attribute from the generated xml")
     *
     * @param attributeName Name of attribute rendered from the ERMA monitor to the pattern xml.
     */
    public synchronized void removeXmlAttribute(String attributeName) {
        List newList = new ArrayList(allowedAttributes.get());
        newList.remove(attributeName);
        allowedAttributes.set(newList);
    }
}
