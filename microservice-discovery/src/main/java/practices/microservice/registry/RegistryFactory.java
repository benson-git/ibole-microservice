/**
 * 
 */
package practices.microservice.registry;

import practices.microservice.common.ServerIdentifier;

/**
 * @author bwang
 *
 */
public interface RegistryFactory <T> {
	
	T getServiceRegistry(ServerIdentifier identifier);
}
