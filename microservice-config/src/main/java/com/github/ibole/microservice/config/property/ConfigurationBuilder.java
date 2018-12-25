package com.github.ibole.microservice.config.property;

import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 *********************************************************************************************/


public class ConfigurationBuilder {

  protected final Logger log = LoggerFactory.getLogger(ConfigurationBuilder.class);

  private final PropertyMap properties = new PropertyMap();

  /**
   * Add all properties from external Map.
   * 
   * @param props the properties to add
   * @return the instance of ConfigurationBuilder
   */
  public ConfigurationBuilder properties(final Map<String, String> props) {
    if (props == null) {
      throw new NullPointerException();
    }
    if (log.isDebugEnabled()) {
      log.debug("Adding properties:");
      for (Entry<String, String> entry : props.entrySet()) {
        log.debug("  {}='{}'", entry.getKey(), entry.getValue());
      }
    }
    this.properties.putAll(props);
    return this;
  }

  /**
   * Add all properties from external URL.
   * 
   * @param url the url to load
   * @return the instance of ConfigurationBuilder
   * @throws IOException exception when url is null
   */
  public ConfigurationBuilder properties(final URL url) throws IOException {
    if (url == null) {
      throw new NullPointerException();
    }
    log.debug("Reading properties from: {}", url);
    PropertyMap props = new PropertyMap();
    props.load(url);
    return properties(props);
  }

  public ConfigurationBuilder properties(final String resource, final boolean required)
      throws IOException {
    return properties(getClass(), resource, required);
  }

  /**
   * @since 3.0
   * @param clazz Class
   * @param resource String
   * @param required boolean
   * @return the instance of ConfigurationBuilder
   * @throws IOException IOException
   */
  public ConfigurationBuilder properties(final Class<?> clazz, final String resource,
      final boolean required) throws IOException {
    URL url = clazz.getResource(resource);
    if (url == null) {
      if (required) {
        throw new IllegalStateException("Missing required resource: " + resource);
      }
      return this;
    }
    return properties(url);
  }

  /**
   * Add all properties from external File.
   * @param resource File
   * @param required boolean
   * @return the instance of ConfigurationBuilder
   * @throws IOException IOException
   */
  public ConfigurationBuilder properties(final File resource, final boolean required)
      throws IOException {
    if (resource == null || !resource.exists()) {
      if (required) {
        throw new IllegalStateException("Missing required resource: " + resource);
      }
      return this;
    }
    return properties(resource.toURI().toURL());
  }

  public ConfigurationBuilder defaults() throws IOException {
    return properties("/default.properties", true);
  }

  /**
   * Set the key/value to the properties.
   * 
   * @param name the property key
   * @param value the property value
   * @return the the instance of ConfigurationBuilder
   */
  public ConfigurationBuilder set(final String name, final String value) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      throw new NullPointerException();
    }
    log.debug("Set: {}={}", name, value);
    properties.put(name, value);
    return this;
  }

  /**
   * Provides customization of configuration.
   */
  public interface Customizer {
    void apply(ConfigurationBuilder builder) throws Exception;
  }

  /**
   * custom configuration builder.
   * 
   * @param customizer the Customizer
   * @return ConfigurationBuilder
   * @throws Exception exception when customizer is null
   */
  public ConfigurationBuilder custom(final Customizer customizer) throws Exception {
    if (customizer == null) {
      throw new NullPointerException();
    }
    log.debug("Customizing: {}", customizer);
    customizer.apply(this);
    return this;
  }

  /**
   * Override any existing properties with values from the given set of overrides.
   * @param overrides Map
   * @return ConfigurationBuilder
   */
  public ConfigurationBuilder override(final Map<String, String> overrides) {
    if (overrides == null) {
      throw new NullPointerException();
    }
    for (Entry<String, String> entry : overrides.entrySet()) {
      String name = entry.getKey();
      if (properties.containsKey(name)) {
        String value = entry.getValue();
        log.debug("Override: {}={}", name, value);
        properties.put(name, value);
      }
    }
    return this;
  }

  /**
   * Override existing properties by the specified properties.
   * @see #override(Map)
   * @param overrides Properties
   * @return ConfigurationBuilder
   */
  public ConfigurationBuilder override(final Properties overrides) {
    return override(new PropertyMap(overrides));
  }

  /**
   * Load external property file and override the existed properties. Normally the external file can
   * be specified in system environment or JVM properties setting.
   * @param name String
   */
  private void loadExternalProperties(final String name) throws IOException {
    String value = properties.get(name);
    if (value == null) {
      value = System.getProperty(JVMSystemProperties.EXTERNAL_CONFIG_PATH.getValue());
    }

    if (value == null) {
      value = System.getenv(SystemEnvironmentVariables.EXTERNAL_CONFIG_PATH.getValue());
    }

    if (value == null) {
      log.warn("Unable to load external properties null entry: {}", name);
      return;
    }

    File file = new File(value).getCanonicalFile();
    
    PropertyMap props = new PropertyMap();
    props.load(file.toURI().toURL());
    
    this.override(props);

    properties.put(name, file.getPath());
  }

  /**
   * underTest.set("foo", "bar"); underTest.set("baz", "${foo}"); assertThat(config.get("foo"),
   * is("bar")); assertThat(config.get("baz"), is("bar"));
   * 
   * @throws Exception Exception
   */
  private void interpolate() throws Exception {

    Interpolator interpolator = new StringSearchInterpolator();
    interpolator.addValueSource(new MapBasedValueSource(properties));
    interpolator.addValueSource(new MapBasedValueSource(System.getProperties()));
    interpolator.addValueSource(new EnvarBasedValueSource());

    for (Entry<String, String> entry : properties.entrySet()) {
      properties.put(entry.getKey(), interpolator.interpolate(entry.getValue()));
    }
  }

  /**
   * Build properties.
   * 
   * @return the properties
   * @throws Exception exception when not configured
   */
  public Map<String, String> build() throws Exception {
    if (properties.isEmpty()) {
      throw new IllegalStateException("Not configured");
    }

    loadExternalProperties("external.properties");

    interpolate();
    
    // return copy
    PropertyMap props = new PropertyMap(properties);
    log.info("Properties at the end:");
    for (String key : props.keys()) {
      log.info("  {}='{}'", key, props.get(key));
    }

    return props;
  }
}
