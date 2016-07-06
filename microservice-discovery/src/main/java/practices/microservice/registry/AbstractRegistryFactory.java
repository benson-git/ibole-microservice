/**
 * 
 */
package practices.microservice.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.AbstractDiscoveryFactory;
import practices.microservice.discovery.InstanceMetadata;

import com.google.common.collect.Maps;

/**
 * Common logic handling for the implementation of {@code DiscoveryFactory}.
 * @author bwang
 * @param <T>
 *
 */
public abstract class AbstractRegistryFactory implements RegistryFactory<ServiceRegistry<InstanceMetadata>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiscoveryFactory.class);
    private static final Map<ServerIdentifier, ServiceRegistry<InstanceMetadata>> REGISTRIES = Maps.newConcurrentMap();
	private static final ReentrantLock LOCK = new ReentrantLock();


    /**
     * Retrieve all registry center handler
     * 
     * @return the collection of ServiceRegistry
     */
    public static Collection<ServiceRegistry<InstanceMetadata>> getServiceDiscoveries() {
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
            for (ServiceRegistry<InstanceMetadata> registry : getServiceDiscoveries()) {
                try {
                    registry.destroy();
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            REGISTRIES.clear();
        } finally {
            //release the lock
            LOCK.unlock();
        }
    }

	public ServiceRegistry<InstanceMetadata> getServiceRegistry(ServerIdentifier identifier) {;
  
        // lock retrieve process
        LOCK.lock();
        try {
        	ServiceRegistry<InstanceMetadata> discovery =  REGISTRIES.get(identifier);
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

	 protected abstract ServiceRegistry<InstanceMetadata> createRegistry(ServerIdentifier identifier);
}
