package practices.microservice.registry;

import java.io.IOException;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.RegisterEntry;

/**
 * 
 * @author bwang
 *
 */
public interface ServiceRegistry<T>{
	
	void start();
	
	void register(RegisterEntry entry);

	void unregisterService(RegisterEntry entry);
	
	void destroy() throws IOException;
	
	ServerIdentifier getIdentifier();
	
}
