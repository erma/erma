package com.orbitz.monitoring.api;

/**
 * An exception indicating that the requested attribute is undefined.
 *
 * @author Doug Barth
 */
public class AttributeUndefinedException extends RuntimeException {
    public AttributeUndefinedException(String attribute) {
        super(attribute + " value is undefined");
    }
}
