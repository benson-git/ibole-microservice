/**
 * 
 */
package practices.microservice.discovery.zookeeper;

import practices.microservice.discovery.DiscoveryFactory;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.ServiceDiscovery;
import practices.microservice.discovery.ServiceDiscoveryProvider;

/**
 * @author bwang
 *
 */
public class ZkServiceDiscoveryProvider extends ServiceDiscoveryProvider {

	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	@Override
	protected int priority() {
		
		return 5;
	}

	@Override
	public DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> getDiscoveryFactory() {
		
		return new ZkDiscoveryFactory();
	}


}
