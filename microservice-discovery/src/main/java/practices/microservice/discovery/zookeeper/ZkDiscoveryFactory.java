/**
 * 
 */
package practices.microservice.discovery.zookeeper;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.AbstractDiscoveryFactory;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.ServiceDiscovery;

/**
 * @author bwang
 *
 */
public class ZkDiscoveryFactory extends AbstractDiscoveryFactory {

	@Override
	protected ServiceDiscovery<InstanceMetadata> createDiscovery(ServerIdentifier identifier) {
	
		return new ZkServiceDiscovery(identifier);
	}

	
}
