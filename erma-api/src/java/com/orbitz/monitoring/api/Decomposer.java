package com.orbitz.monitoring.api;

import java.io.Serializable;

/**
 * The contract for classes that takes objects that can not be safely sent
 * across the network and turns them into objects that can be sent.<p>
 *
 * The underlying implementation of the new objects is not defined. There are
 * two requirements for the object that is to replace the original object when
 * sent over the network:
 * <ul>
 * <li> All JavaBean properties must be available in the returned object
 * <li> The resulting object must be able to be introspected when the
 *    {@link Monitor#get(String)} method is called with an expression
 *    for a key
 * </ul>
 *
 * @author Doug Barth
 */
public interface Decomposer {
  /**
   * Takes an object that may not be able to be sent across the wire and turns
   * it into one that can be.
   *
   * @param object the object to transform
   * @return the Serializable version of this object
   */
  Serializable decompose(Object object);
}
