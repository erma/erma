package com.orbitz.monitoring.api.monitor;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CompositeAttributeMap provides the additional functionality to
 * have inheritable attributes
 */
public class CompositeAttributeMap extends AttributeMap {
    private static final Logger logger = Logger.getLogger(CompositeAttributeMap.class);

    /**
     * Hard version used to support evolutionary compatibility during
     * serialization between client and server.  Basically, an older instance
     * of this class can be compatible with a newer one, if the new additions
     * are optional / ancillary to core functionality / backward compatible.
     *
     * NOTE: Changing this value requires coordination with other teams.
     *       !! TREAD LIGHTLY !!
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public CompositeAttributeMap() {
        super();
    }

    /**
     * Constructor.
     *
     * @param attributes initial attributes
     */
    public CompositeAttributeMap(AttributeMap attributes) {
        super(attributes.getAll());
    }

    /**
     * Given a map of key -> CompositeAttributeHolder, clone and add all entries to
     * this attributes map.  Overriding b/c we need to put CAH in attributes.
     *
     * @param attributeHolders map of key -> CompositeAttributeHolder
     */
    public void setAllAttributeHolders(Map attributeHolders) {
        if (attributeHolders == null) return;

        for (Iterator i = attributeHolders.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            
            if (CompositeAttributeHolder.class.isAssignableFrom(value.getClass())) {
                CompositeAttributeHolder original = (CompositeAttributeHolder) value;
                CompositeAttributeHolder copy = (CompositeAttributeHolder) original.clone();
                getAttributes().put(key, copy);
            } else if (AttributeHolder.class.isAssignableFrom(value.getClass())) {
                AttributeHolder original = (AttributeHolder) value;
                CompositeAttributeHolder copy = new CompositeAttributeHolder(original.getValue());
                if (original.isSerializable()) copy.serializable();
                if (original.isLocked()) copy.lock();
                getAttributes().put(key, copy);
            } else {
                set(key, value);
            }
        }
    }

    /**
     * Generate a new map of key -> value containing all inheritable attributes
     * @return map of inheritable attributes
     */
    public Map getAllInheritable() {
        Map allInheritable = new HashMap();
        for (Iterator i = getAttributes().entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            CompositeAttributeHolder attributeHolder = (CompositeAttributeHolder) entry.getValue();

            if (attributeHolder.isInheritable()) {
                Object value = attributeHolder.getValue();
                allInheritable.put(key, value);
            }
        }
        return allInheritable;
    }

    /**
     * Generate a new map of key -> CompsositeAttributeHolder containing all inheritable attributes
     * @return map of inheritable attributes
     */
    public Map getAllInheritableAttributeHolders() {
        Map allInheritable = new HashMap();
        for (Iterator i = getAllAttributeHolders().entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            CompositeAttributeHolder attributeHolder = (CompositeAttributeHolder) entry.getValue();
            
            if (attributeHolder.isInheritable()) {
                allInheritable.put(key, attributeHolder);
            }
        }
        return allInheritable;
    }

    protected AttributeHolder createHolderForValue(AttributeHolder old, Object value) {
        CompositeAttributeHolder attributeHolder = new CompositeAttributeHolder(value);
        if (old.isSerializable()) attributeHolder.serializable();
        if (CompositeAttributeHolder.class.isAssignableFrom(old.getClass())) {
            if (((CompositeAttributeHolder)old).isInheritable()) attributeHolder.setInheritable(true);
        }
        return attributeHolder;
    }
    protected AttributeHolder createHolderForValue(Object value) {
        return new CompositeAttributeHolder(value);
    }
}
