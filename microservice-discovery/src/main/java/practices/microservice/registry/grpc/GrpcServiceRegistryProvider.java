/**
 * 
 */
package practices.microservice.registry.grpc;

import io.grpc.ServerServiceDefinition;
import practices.microservice.registry.ServiceRegistry;
import practices.microservice.registry.ServiceRegistryProvider;

/**
 * @author bwang
 *
 */
public class GrpcServiceRegistryProvider extends ServiceRegistryProvider {

	/* (non-Javadoc)
	 * @see practices.rpc.service.registry.ServiceRegistryProvider#isAvailable()
	 */
	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see practices.rpc.service.registry.ServiceRegistryProvider#priority()
	 */
	@Override
	protected int priority() {
		
		return 5;
	}

	/* (non-Javadoc)
	 * @see practices.rpc.service.registry.ServiceRegistryProvider#createServiceRegistry()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ServiceRegistry<ServerServiceDefinition> createServiceRegistry() {
		//TODO
		return null;
	}

}
