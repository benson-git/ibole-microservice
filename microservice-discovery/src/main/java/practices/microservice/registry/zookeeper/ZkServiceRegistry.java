/**
 * 
 */
package practices.microservice.registry.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.registry.AbstractServiceRegistry;
import practices.microservice.registry.RegisterEntry;

/**
 * @author bwang
 *
 */
public class ZkServiceRegistry extends AbstractServiceRegistry<RegisterEntry> {

	CuratorFramework client = null;
	
	public ZkServiceRegistry(ServerIdentifier identifier) {
		super(identifier);
	}

	@Override
	public void start() throws IOException {
		client = CuratorFrameworkFactory.newClient(getIdentifier()
				.getConnectionString(), new ExponentialBackoffRetry(1000, 3));
	}

	@Override
	public void register(RegisterEntry instance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerList(List<RegisterEntry> instanceList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RegisterEntry> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RegisterEntry getInstance(String path, String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(RegisterEntry entry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(List<RegisterEntry> instanceList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
}
