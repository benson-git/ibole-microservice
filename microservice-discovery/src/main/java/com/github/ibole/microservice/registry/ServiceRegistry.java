package com.github.ibole.microservice.registry;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.RegisterEntry;

import java.io.IOException;

/**
 * Service Registry.
 * @author bwang
 *
 */
public interface ServiceRegistry<T> {

  void start();

  void register(RegisterEntry entry);

  void unregisterService(String serviceName);

  void destroy() throws IOException;

  ServerIdentifier getIdentifier();

}
