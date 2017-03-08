package com.github.ibole.microservice.registry.zookeeper;

import com.github.ibole.microservice.registry.ServiceRegistryProvider;

/**
 * The provider to get zookeeper registry factory.
 * @author bwang
 *
 */
public class ZkServiceRegistryProvider extends ServiceRegistryProvider {

  @Override
  protected boolean isAvailable() {

    return true;
  }

  @Override
  protected int priority() {

    return 5;
  }

  @Override
  public ZkRegistryFactory getRegistryFactory() {

    return new ZkRegistryFactory();
  }


}
