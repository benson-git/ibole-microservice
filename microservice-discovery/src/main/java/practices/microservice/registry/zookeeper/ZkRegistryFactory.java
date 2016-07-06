/**
 * 
 */
package practices.microservice.registry.zookeeper;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.registry.AbstractRegistryFactory;
import practices.microservice.registry.ServiceRegistry;

/**
 * @author bwang
 *
 */
public class ZkRegistryFactory extends AbstractRegistryFactory {

	@Override
	protected ServiceRegistry<InstanceMetadata> createRegistry(ServerIdentifier identifier) {
	
		return new ZkServiceRegistry(identifier);
	}

	
}
