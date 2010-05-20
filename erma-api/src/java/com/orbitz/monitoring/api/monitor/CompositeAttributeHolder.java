package com.orbitz.monitoring.api.monitor;

/**
 * CompositeAttributeHolder adds the inheritable meta-data to
 * attributes.
 */
public class CompositeAttributeHolder extends AttributeHolder {

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

    private boolean inheritable = false;

    public CompositeAttributeHolder(final Object value) {
        super(value);
    }

    public CompositeAttributeHolder(final Object value, final boolean inheritable) {
        super(value);
        this.inheritable = inheritable;
    }

    public boolean isInheritable() {
        return inheritable;
    }

    public CompositeAttributeHolder setInheritable(final boolean inheritable) {
        this.inheritable = inheritable;
        return this;
    }

    public Object clone() {
        return super.clone();
    }
}
