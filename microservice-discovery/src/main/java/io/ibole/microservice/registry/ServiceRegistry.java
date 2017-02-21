package io.ibole.microservice.registry;

import io.ibole.microservice.common.ServerIdentifier;
import io.ibole.microservice.discovery.RegisterEntry;

import java.io.IOException;

/**
 * Service Registry.
 * @author bwang
 *
 */
public interface ServiceRegistry<T> {

  void start();

  void register(RegisterEntry entry);

  void unregisterService(RegisterEntry entry);

  void destroy() throws IOException;

  ServerIdentifier getIdentifier();

}
