package com.github.ibole.microservice.discovery;

import com.github.ibole.microservice.common.ServerIdentifier;


/**
 * The factory is to create discovery&registry handler for specific registry center as
 * multi-registry center coexistence is supported by discovery module.
 * 
 *<p>Like Zookeeper registry center handler and Redis registry center handler.
 * 
 * @author bwang
 *
 */
public interface DiscoveryFactory<T> {

  T getServiceDiscovery(ServerIdentifier identifier);
}
