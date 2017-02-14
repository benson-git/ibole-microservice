package io.ibole.microservice.registry;

import io.ibole.microservice.common.ServerIdentifier;

/**
 * Registry Factory.
 * @author bwang
 *
 */
public interface RegistryFactory<T> {

  T getServiceRegistry(ServerIdentifier identifier);
}
