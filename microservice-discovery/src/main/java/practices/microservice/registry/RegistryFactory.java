package practices.microservice.registry;

import practices.microservice.common.ServerIdentifier;


/**
 * The factory is to create registry center handler for specific registry center as multi-registry center coexistence is supported by discovery module. 
 * 
 * Like Zookeeper registry center handler and Redis registry center handler.
 * 
 *  .
 * 
 * @author bwang
 *
 */
public interface RegistryFactory<T> {
	
	ServiceRegistry<T> getServiceRegistry(ServerIdentifier identifier);
}
