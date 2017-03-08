package com.github.ibole.microservice.registry;

import com.github.ibole.microservice.common.ServerIdentifier;

/**
 * Registry Factory.
 * @author bwang
 *
 */
public interface RegistryFactory<T> {

  T getServiceRegistry(ServerIdentifier identifier);
}
