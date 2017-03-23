package com.github.ibole.microservice.registry;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.AbstractDiscoveryFactory;
import com.github.ibole.microservice.discovery.HostMetadata;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Common logic handling for the implementation of {@code DiscoveryFactory}.
 * 统一处理zk服务的关闭.
 * 通过ZooKeeper发布服务，服务启动时将自己的信息注册为临时节点，当服务断掉时ZooKeeper将此临时节点删除，
 * 这样client就不会得到服务的信息了.
 * 
 * @author bwang
 *
 */
public abstract class AbstractRegistryFactory
    implements RegistryFactory<ServiceRegistry<HostMetadata>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveryFactory.class);
  private static final Map<ServerIdentifier, ServiceRegistry<HostMetadata>> REGISTRIES =
      Maps.newConcurrentMap();
  private static final ReentrantLock LOCK = new ReentrantLock();


  /**
   * Retrieve all registry center handler.
   * 
   * @return the collection of ServiceRegistry
   */
  public static Collection<ServiceRegistry<HostMetadata>> getServiceDiscoveries() {
    return Collections.unmodifiableCollection(REGISTRIES.values());
  }

  /**
   * Destroy all registry center handler.
   */
  public static void destroyAll() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Close all registries " + getServiceDiscoveries());
    }
    // lock the whole close process
    LOCK.lock();
    try {
      for (ServiceRegistry<HostMetadata> registry : getServiceDiscoveries()) {
        try {
          registry.destroy();
        } catch (Throwable e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
      REGISTRIES.clear();
    } finally {
      // release the lock
      LOCK.unlock();
    }
  }

  /**
   * Get service registry.
   */
  public ServiceRegistry<HostMetadata> getServiceRegistry(ServerIdentifier identifier) {
    ;

    // lock retrieve process
    LOCK.lock();
    try {
      ServiceRegistry<HostMetadata> discovery = REGISTRIES.get(identifier);
      if (discovery != null) {
        return discovery;
      }
      discovery = createRegistry(identifier);
      if (discovery == null) {
        throw new IllegalStateException("Can not create registry " + identifier.toString());
      }
      REGISTRIES.put(identifier, discovery);
      return discovery;
    } finally {
      // release lock
      LOCK.unlock();
    }
  }

  protected abstract ServiceRegistry<HostMetadata> createRegistry(ServerIdentifier identifier);
}
