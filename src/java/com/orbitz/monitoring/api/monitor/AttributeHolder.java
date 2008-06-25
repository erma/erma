package com.orbitz.monitoring.api.monitor;

import java.io.Serializable;

/**
 * Holds a Monitor attribute value and associated metadata.
 *
 * <p/>
 */
public class AttributeHolder implements Serializable, Cloneable {

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

    private boolean serializable = false;
    private boolean locked = false;

    private Object value;

    public AttributeHolder(Object value) {
        this.value = value;
    }

    /**
     * The serializable property of attributes applies to generating serializable
     * momentos for Monitor objects.   
     *
     * @return true if this attribute should be included in a monitor's serializable momento
     * @see com.orbitz.monitoring.api.Monitor#getSerializableMomento()
     */
    public boolean isSerializable() {
        return serializable;
    }

    /**
     * Attempt to mark this attribute as serializable.  The value of this
     * AttributeHolder must implement the Serializable interface for this
     * attribute to be marked as serializable.  Calling this method on an
     * attribute that does not implement java.io.Serializable will do nothing.
     *
     * @return reference to this object
     * @see com.orbitz.monitoring.api.Monitor#getSerializableMomento()
     */
    public AttributeHolder serializable() {
        serializable = value != null && Serializable.class.isAssignableFrom(value.getClass());
        //failing silently if set(foo,bar).serializable() is not serializable
        return this;
    }

    /**
     * Set this AttributeHolder as not serializable.  By default, primitive
     * types and Strings are serializable; this method can be used to
     * revert that.
     *
     * @return reference to this AttributeHolder
     * @see com.orbitz.monitoring.api.Monitor#getSerializableMomento()
     */
    public AttributeHolder notSerializable() {
        serializable = false;
        return this;
    }

    /**
     * A locked attribute on a Monitor cannot be overriden with a new value.
     *
     * @return true if the attribute has been locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Prevent this attribute value from being overriden later.
     *
     * @return reference to this AttributeHolder
     */
    public AttributeHolder lock() {
        this.locked = true;
        return this;
    }

    /**
     * Get the value for the attribute.
     *
     * @return attribute value
     */
    public Object getValue() {
        return value;
    }

    public String toString() {
        return (value != null) ? value.toString() : "null";
    }

    /**
     * Two AttributeHolders are equal if their values are equal.
     *
     * @param o AttributeHolder to compare to this object
     * @return true if both AttributeHolders have the same value
     */
    public boolean equals(Object o) {
        if (! (o instanceof AttributeHolder)) {
            return false;
        }

        Object v1 = this.getValue();
        Object v2 = ((AttributeHolder)o).getValue();

        if ((v1 == null) && (v2 == null)) return true;

        if ((v1 == null) || (v2 == null)) return false;

        return (v1.equals(v2));
    }

    public int hashCode() {
        return (value == null) ? "null".hashCode() : value.hashCode();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // should never happen as this class is Cloneable
            throw new RuntimeException(e);
        }
    }
}
