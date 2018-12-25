package com.github.ibole.microservice.config.property;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/

/**
 * Properties-like map with appropriate generics signature.
 *
 */
public class PropertyMap extends HashMap<String, String> {


  private static final long serialVersionUID = 2734081461296533186L;

  public PropertyMap() {
    super();
  }

  public PropertyMap(final Map<String, String> map) {
    super(map);
  }

  /**
   * Constructor with parma type Properties.
   * @param properties Properties
   */
  public PropertyMap(final Properties properties) {
    putAll(properties);
  }

  /**
   * Put all key/value from properties
   * 
   * @param props the Properties.
   */
  public void putAll(final Properties props) {
    for (Entry<Object, Object> entry : props.entrySet()) {
      put(entry.getKey().toString(), String.valueOf(entry.getValue()));
    }
  }

  /**
   * Get value with the specified key.
   * 
   * @param key key
   * @param defaultValue default Value
   * @return the value with the specified key.
   */
  public String get(final String key, final String defaultValue) {
    String value = super.get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Load properties with the specified input stream.
   * 
   * @param input InputStream
   * @throws IOException IOException.
   */
  public void load(final InputStream input) throws IOException {
    Properties p = new Properties();
    p.load(input);
    putAll(p);
  }

  /**
   * Load properties with the specified URL.
   * @param url URL 
   * @throws IOException IOException.
   */
  public void load(final URL url) throws IOException {
    try (InputStream input = url.openStream()) {
      load(input);
    }
  }

  /**
   * List of sorted keys.
   * @return list of sorted keys
   */
  public List<String> keys() {
    List<String> keys = new ArrayList<>(keySet());
    Collections.sort(keys);
    return Collections.unmodifiableList(keys);
  }
}
