package io.ibole.microservice.discovery;

import io.ibole.microservice.common.ServerIdentifier;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The abstract of DiscoveryFactory.
 * 
 * @author bwang
 *
 */
public abstract class AbstractDiscoveryFactory
    implements DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveryFactory.class);
  private static final Map<ServerIdentifier, ServiceDiscovery<InstanceMetadata>> REPOSITION =
      Maps.newConcurrentMap();
  private static final ReentrantLock LOCK = new ReentrantLock();


  /**
   * Retrieve all registry center handler.
   * 
   * @return the collection of ServiceRegistry
   */
  public static Collection<? extends ServiceDiscovery<InstanceMetadata>> getServiceDiscoveries() {
    return Collections.unmodifiableCollection(REPOSITION.values());
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
      for (ServiceDiscovery<InstanceMetadata> registry : getServiceDiscoveries()) {
        try {
          registry.destroy();
        } catch (Throwable e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
      REPOSITION.clear();
    } finally {
      // release the lock
      LOCK.unlock();
    }
  }

  /**
   * Get Service Discovery.
   */
  public ServiceDiscovery<InstanceMetadata> getServiceDiscovery(ServerIdentifier identifier) {

    // lock retrieve process
    LOCK.lock();
    try {
      ServiceDiscovery<InstanceMetadata> discovery = REPOSITION.get(identifier);
      if (discovery != null) {
        return discovery;
      }
      discovery = createDiscovery(identifier);
      if (discovery == null) {
        throw new IllegalStateException("Can not create registry " + identifier.toString());
      }
      REPOSITION.put(identifier, discovery);
      return discovery;
    } finally {
      // release lock
      LOCK.unlock();
    }
  }

  protected abstract ServiceDiscovery<InstanceMetadata> createDiscovery(
      ServerIdentifier identifier);
}
