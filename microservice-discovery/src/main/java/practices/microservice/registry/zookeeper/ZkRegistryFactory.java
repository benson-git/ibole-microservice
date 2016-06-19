/**
 * 
 */
package practices.microservice.registry.zookeeper;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.registry.AbstractRegistryFactory;
import practices.microservice.registry.InstanceMetadata;
import practices.microservice.registry.ServiceRegistry;

/**
 * @author bwang
 *
 */
public class ZkRegistryFactory extends AbstractRegistryFactory {

	/* (non-Javadoc)
	 * @see practices.microservice.registry.AbstractRegistryFactory#createRegistry(practices.microservice.registry.ServerIdentifier)
	 */
	@Override
	protected ServiceRegistry<InstanceMetadata> createRegistry(ServerIdentifier identifier) {
		
		return new ZkServiceRegistry(identifier);
	}

}
