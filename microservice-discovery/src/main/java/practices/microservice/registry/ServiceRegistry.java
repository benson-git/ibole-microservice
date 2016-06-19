package practices.microservice.registry;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.registry.RegisterEntry.ServiceType;
/**
 * 
 * @author bwang
 *
 */
public interface ServiceRegistry<T> extends Closeable{
	
	void start() throws IOException;
	
	void register(RegisterEntry entry);
	
	List<T> listAll(ServiceType type, String serviceName, String serviceContract);
	
	T getInstance(ServiceType type, String serviceName, String serviceContract);
	
	void destroy();
	
	ServerIdentifier getIdentifier();

	void updateService(RegisterEntry entry);

	void unregisterService(RegisterEntry entry);
	
}
