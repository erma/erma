package com.orbitz.monitoring.test;

/**
 * Description of class goes here.<p>
 * <p/>
 */
public class OC {
  private Object _object;
  private Object _privateObject = new Object();

  public OC(Object object) {
    _object = object;
  }

  public Object getObject() {
    return _object;
  }

  public Object getExceptionThrower() {
    throw new RuntimeException("ExceptionThrower");
  }

  private Object getPrivateObject() {
    return _privateObject;
  }
}
