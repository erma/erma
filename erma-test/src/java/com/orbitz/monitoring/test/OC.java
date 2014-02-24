package com.orbitz.monitoring.test;

/**
 * Description of class goes here.<p>
 * <p/>
 */
public class OC {
  private Object object;
  private Object privateObject = new Object();

  public OC(Object object) {
    this.object = object;
  }

  public Object getObject() {
    return object;
  }

  public Object getExceptionThrower() {
    throw new RuntimeException("ExceptionThrower");
  }

  private Object getPrivateObject() {
    return privateObject;
  }
}
