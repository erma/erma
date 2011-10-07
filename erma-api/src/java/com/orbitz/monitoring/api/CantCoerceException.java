package com.orbitz.monitoring.api;

/**
 * Indicates that an attribute is of a different type than was expected and can't be converted into
 * the correct type
 * @author Doug Barth
 */
public class CantCoerceException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  /**
   * Creates a {@link CantCoerceException}
   * @param key the {@link Monitor} attribute's key
   * @param value the attribute's value
   * @param coercingTo describes the type to which the value was expected to be, probably the
   *        canonical class name
   */
  public CantCoerceException(final String key, final Object value, final String coercingTo) {
    super(key + " can't be made into a " + coercingTo + "; is " + value.getClass() + ": " + value);
  }
}
