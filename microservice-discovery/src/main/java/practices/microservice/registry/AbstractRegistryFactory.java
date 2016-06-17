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

import com.google.common.collect.Maps;

/**
 * Common logic handling for the implementation of {@code RegistryFactory}.
 * @author bwang
 *
 */
public abstract class AbstractRegistryFactory implements RegistryFactory<RegisterEntry>{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistryFactory.class);
	
	private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Map<ServerIdentifier, ServiceRegistry<RegisterEntry>> REGISTRY = Maps.newConcurrentMap();
    
    /**
     * Retrieve all registry center handler
     * 
     * @return the collection of ServiceRegistry
     */
    public static Collection<ServiceRegistry<RegisterEntry>> getServiceRegistries() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Destroy all registry center handler.
     */
    public static void destroyAll() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Close all registries " + getServiceRegistries());
        }
        // lock the whole close process
        LOCK.lock();
        try {
            for (ServiceRegistry<RegisterEntry> registry : getServiceRegistries()) {
                try {
                    registry.destroy();
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            REGISTRY.clear();
        } finally {
            //release the lock
            LOCK.unlock();
        }
    }

    public ServiceRegistry<RegisterEntry> getServiceRegistry(ServerIdentifier identifier) {;
  
        // lock retrieve process
        LOCK.lock();
        try {
        	ServiceRegistry<RegisterEntry> registry = REGISTRY.get(identifier);
            if (registry != null) {
                return registry;
            }
            registry = createRegistry(identifier);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + identifier.toString());
            }
            REGISTRY.put(identifier, registry);
            return registry;
        } finally {
            // release lock
            LOCK.unlock();
        }
    }

    protected abstract ServiceRegistry<RegisterEntry> createRegistry(ServerIdentifier identifier);
}
