/**
 * 
 */
package practices.microservice.registry.zookeeper;

import practices.microservice.registry.ServiceRegistryProvider;

/**
 * @author bwang
 *
 */
public class ZkServiceRegistryProvider extends ServiceRegistryProvider {

	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	@Override
	protected int priority() {
		
		return 5;
	}

	@Override
	public ZkRegistryFactory getRegistryFactory() {
		
		return new ZkRegistryFactory();
	}


}
