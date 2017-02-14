package io.ibole.microservice.registry.zookeeper;

import io.ibole.microservice.common.ServerIdentifier;
import io.ibole.microservice.discovery.InstanceMetadata;
import io.ibole.microservice.registry.AbstractRegistryFactory;
import io.ibole.microservice.registry.ServiceRegistry;

/**
 * Zookeeper registry factory.
 * @author bwang
 *
 */
public class ZkRegistryFactory extends AbstractRegistryFactory {

  @Override
  protected ServiceRegistry<InstanceMetadata> createRegistry(ServerIdentifier identifier) {

    return new ZkServiceRegistry(identifier);
  }


}
