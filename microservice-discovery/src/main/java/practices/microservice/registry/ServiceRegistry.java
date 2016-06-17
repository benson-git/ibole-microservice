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
	
	void start() throws IOException;
	
	void register(T instance);
	
	void registerList(List<T> instanceList);
	
	List<T> listAll();
	
	//T getInstanceWithStrategy(String path, String serviceName, ProviderStrategy<T> strategy);
	
	T getInstance(String path, String serviceName);
	
	void delete(T entry);
	
	void deleteAll(List<T> instanceList);
	
	void destroy();
	
	ServerIdentifier getIdentifier();
	
}
