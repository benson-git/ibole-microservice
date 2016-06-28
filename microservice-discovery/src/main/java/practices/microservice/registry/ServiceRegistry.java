package practices.microservice.registry;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import practices.microservice.common.ServerIdentifier;
/**
 * 
 * @author bwang
 *
 */
public interface ServiceRegistry<T> extends Closeable{
	
	void start() throws Exception;
	
	void register(RegisterEntry entry) throws Exception;
	
	List<T> listAll(String serviceContract);
	
	T getInstance(String serviceContract);
	
	T getInstanceById(String serviceContract, String id);
	
	void destroy() throws IOException;
	
	ServerIdentifier getIdentifier();

	void updateService(RegisterEntry entry);

	void unregisterService(RegisterEntry entry) throws Exception;
	
}
