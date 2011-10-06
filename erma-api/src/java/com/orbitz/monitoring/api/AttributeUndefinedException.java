package com.orbitz.monitoring.api;

/**
 * An exception indicating that a requested attribute is undefined.
 * @author Doug Barth
 */
public class AttributeUndefinedException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  /**
   * Creates an attribute undefined exception
   * @param attributeName the name of the attribute
   */
  public AttributeUndefinedException(final String attributeName) {
    super(attributeName + " value is undefined");
  }
}
