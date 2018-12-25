package com.github.ibole.microservice.config.property;

import java.util.Map;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * Holder for bootstrap configuration properties.
 *
 */
public final class ConfigurationHolder {
  
  private static Map<String, String> props;

  private ConfigurationHolder() {
    // empty
  }

  public static void set(final Map<String, String> properties) {
    props = properties;
  }

  public static Map<String, String> get() {
    return props;
  }

  public static void unset() {
    props.clear();
    props = null;
  }
}

