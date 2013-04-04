package com.orbitz.monitoring.api.monitor;

import com.google.common.base.Objects;

/**
 * CompositeAttributeHolder adds the inheritable meta-data to attributes.
 */
public class CompositeAttributeHolder extends AttributeHolder {
  private static final long serialVersionUID = 1L;
  
  private boolean inheritable = false;
  
  /**
   * Creates a {@link CompositeAttributeHolder} holding a specified value that is <b>not</b>
   * inheritable
   * @param value the value to hold
   */
  public CompositeAttributeHolder(final Object value) {
    super(value);
  }
  
  /**
   * Creates a {@link CompositeAttributeHolder} holding a specified value that may or may not be
   * inheritable
   * @param value the value to hold
   * @param inheritable true if the holder should be inheritable, false otherwise
   */
  public CompositeAttributeHolder(final Object value, final boolean inheritable) {
    super(value);
    this.inheritable = inheritable;
  }
  
  @Override
  public Object clone() {
    return super.clone();
  }
  
  @Override
  public boolean equals(final Object o) {
    if (o instanceof CompositeAttributeHolder) {
      CompositeAttributeHolder that = (CompositeAttributeHolder)o;
      return Objects.equal(this.inheritable, that.inheritable) && super.equals(that);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(this.inheritable, super.hashCode());
  }
  
  /**
   * Indicates whether this holder is inheritable
   * @return true if it is inheritable, false otherwise
   */
  public boolean isInheritable() {
    return inheritable;
  }
  
  /**
   * Sets a value indicating whether the holder is inheritable
   * @param inheritable true if it is inheritable, false otherwise
   * @return this {@link CompositeAttributeHolder}
   */
  public CompositeAttributeHolder setInheritable(final boolean inheritable) {
    this.inheritable = inheritable;
    return this;
  }
}
