package com.github.ibole.microservice.common;


/**
 * Turn on tls or turn off tls.
 * 
 * @author bwang
 *
 */
public enum TLS {

  OFF(1),

  ON(0),

  UNKNOWN(-1);

  private int value;

  private TLS(int enableFlag) {
    value = enableFlag;
  }

  public boolean isOn() {
    return value == 1 ? true : false;
  }

  public boolean isOff() {
    return value == 0 ? true : false;
  }

  public boolean isUnknown() {
    return value == -1 ? true : false;
  }
}
