package io.ibole.microservice.discovery.zookeeper;

import io.ibole.microservice.common.ServerIdentifier;
import io.ibole.microservice.discovery.AbstractDiscoveryFactory;
import io.ibole.microservice.discovery.InstanceMetadata;
import io.ibole.microservice.discovery.ServiceDiscovery;

/**
 * Zookeeper Discovery Factory.
 * @author bwang
 *
 */
public class ZkDiscoveryFactory extends AbstractDiscoveryFactory {

  @Override
  protected ServiceDiscovery<InstanceMetadata> createDiscovery(ServerIdentifier identifier) {

    return new ZkServiceDiscovery(identifier);
  }


}
