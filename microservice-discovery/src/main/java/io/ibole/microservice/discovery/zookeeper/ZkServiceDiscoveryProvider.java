package io.ibole.microservice.discovery.zookeeper;

import io.ibole.microservice.discovery.DiscoveryFactory;
import io.ibole.microservice.discovery.InstanceMetadata;
import io.ibole.microservice.discovery.ServiceDiscovery;
import io.ibole.microservice.discovery.ServiceDiscoveryProvider;

/**
 * Zookeeper Service Discovery Provider.
 * @author bwang
 *
 */
public class ZkServiceDiscoveryProvider extends ServiceDiscoveryProvider {

  @Override
  protected boolean isAvailable() {

    return true;
  }

  @Override
  protected int priority() {

    return 5;
  }

  @Override
  public DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> getDiscoveryFactory() {

    return new ZkDiscoveryFactory();
  }


}
