package com.orbitz.monitoring.api;

/**
 * An exception used to indicate that the attribute is of the wrong type than
 * should be returned.
 *
 * @author Doug Barth
 */
public class CantCoerceException extends RuntimeException {
    public CantCoerceException(String key, Object value, String coercingTo) {
        super(key + " can't be made into a " + coercingTo + "; is "
                + value.getClass() + ": " + value);
    }
}
