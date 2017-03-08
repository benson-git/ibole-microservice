package com.github.ibole.microservice.discovery;

import com.github.ibole.microservice.common.ServerIdentifier;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * The interface of ServiceDiscovery.
 * @author bwang
 *
 */
public interface ServiceDiscovery<T> extends Closeable {

  void start();

  List<T> listAll(String serviceContract);

  T getInstance(String serviceContract);

  T getInstanceById(String serviceContract, String id);

  void destroy() throws IOException;

  void addListener(ServiceRegistryChangeListener listener);

  ServerIdentifier getIdentifier();
}
