package com.github.ibole.microservice.discovery.zookeeper;

import com.github.ibole.microservice.discovery.DiscoveryFactory;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.ServiceDiscovery;
import com.github.ibole.microservice.discovery.ServiceDiscoveryProvider;

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
  public DiscoveryFactory<ServiceDiscovery<HostMetadata>> getDiscoveryFactory() {

    return new ZkDiscoveryFactory();
  }


}
