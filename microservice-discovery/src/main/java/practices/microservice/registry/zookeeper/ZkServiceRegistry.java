/**
 * 
 */
package practices.microservice.registry.zookeeper;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.registry.AbstractServiceRegistry;
import practices.microservice.registry.InstanceMetadata;
import practices.microservice.registry.RegisterEntry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author bwang
 *
 */
public class ZkServiceRegistry extends AbstractServiceRegistry<InstanceMetadata> {

	private CuratorFramework client = null;
	private ServiceDiscovery<InstanceMetadata> serviceDiscovery;
	private Map<String, ServiceProvider<InstanceMetadata>> providers = Maps.newHashMap();
	private List<Closeable> closeableList = Lists.newArrayList();
	private JsonInstanceSerializer<InstanceMetadata> serializer;
	private PathChildrenCache cache = null;
    private InterProcessSemaphoreMutex lock;

	public ZkServiceRegistry(ServerIdentifier identifier) {
		super(identifier);
	}

	@Override
	public void start() throws Exception {
        //1000ms - initial amount of time to wait between retries
	    //3 times - max number of times to retry
		client = CuratorFrameworkFactory.newClient(getIdentifier()
				.getConnectionString(), new ExponentialBackoffRetry(1000, 3));
		client.start();
		client.getZookeeperClient().blockUntilConnectedOrTimedOut();
		lock = new InterProcessSemaphoreMutex(client, this.getIdentifier().getBaseKey());
		 
		serializer = new JsonInstanceSerializer<InstanceMetadata>(InstanceMetadata.class);
		serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
					.basePath(this.getIdentifier().getBaseKey()).client(client).serializer(serializer).build();
		
        cache = new PathChildrenCache(client, this.getIdentifier().getBaseKey(), true);
        try {
			cache.start();
		} catch (Exception e) {
			LOGGER.error("Start cache children path error happened for {}!", this.getIdentifier().getBaseKey(), e);
			throw new RuntimeException(e);
		} 
	}

	@Override
	public void register(RegisterEntry instance) throws Exception {
		lock.acquire(3, TimeUnit.SECONDS);
		try {
			ServiceInstance<InstanceMetadata> thisInstance = ServiceInstance.<InstanceMetadata>builder()
			    .name(instance.getServiceContract())
			    .id(instance.getInstanceMetadata().getId().toString())
			    .payload(instance.getInstanceMetadata()).serviceType(ServiceType.PERMANENT)
			    .build();
			
			serviceDiscovery.start();
			serviceDiscovery.registerService(thisInstance);
			
		} catch (Exception e) {
			LOGGER.error("Register instance {} error happened!", instance.toString(), e);
			throw new RuntimeException(e);
		} 
		finally{
			 lock.release();
		}

	}

	@Override
	public List<InstanceMetadata> listAll(String serviceContract) {
		Collection<ServiceInstance<InstanceMetadata>> instances = null;
        List<InstanceMetadata> metadatum = Lists.newArrayList();
		try {
			serviceDiscovery.start();
			instances = serviceDiscovery.queryForInstances(serviceContract);
			for(ServiceInstance<InstanceMetadata> serviceInstance : instances){
				metadatum.add(serviceInstance.getPayload());
			}
			
		} catch (Exception e) {
			LOGGER.error(
					"List all instances error happened with path '{}'!", this.getIdentifier().getBaseKey()+"/"+serviceContract, e);
			throw new RuntimeException(e);
			
		} 
			
		return metadatum;
	}
    /**
     * Get the instance of InstanceMetadata with the Round Robin strategy.
     * @return the instance of InstanceMetadata.
     */
	@Override
	public InstanceMetadata getInstance(String serviceContract) {
		ServiceProvider<InstanceMetadata> provider = providers.get(serviceContract);
		InstanceMetadata instance = null;
        try {
    		if(provider == null)
    		{
    			provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceContract).providerStrategy(new RoundRobinStrategy<InstanceMetadata>()).build();
    			provider.start();
    			closeableList.add(provider);
                providers.put(serviceContract, provider);
    		}	 
    		instance = provider.getInstance().getPayload();
    		 
		} catch (Exception e) {
			LOGGER.error("Retrieve instance error happened with with path '{}'!", this.getIdentifier().getBaseKey()+"/"+serviceContract, e);
			throw new RuntimeException(e);
		} 
		return instance;
	}

	public InstanceMetadata getInstanceById(String serviceContract, String id) {
		InstanceMetadata metadata = null;
        try {
        	ServiceInstance<InstanceMetadata> instance = serviceDiscovery.queryForInstance(serviceContract, id);
        	metadata = instance.getPayload();
		} catch (Exception e) {
			LOGGER.error("Retrieve instance error happened with '{}'!", this.getIdentifier().getBaseKey()+"/"+serviceContract, e);
			throw new RuntimeException(e);
		} 
		return metadata;
	}
	
	@Override
	public void updateService(RegisterEntry entry) {

		ServiceInstance<InstanceMetadata> thisInstance;
		try {
			thisInstance = ServiceInstance.<InstanceMetadata> builder()
					.name(entry.getServiceContract()).id(entry.getInstanceMetadata().getId().toString())
					.payload(entry.getInstanceMetadata()).build();
			serviceDiscovery.updateService(thisInstance);
		} catch (Exception e) {
			LOGGER.error(
					"Update instance error happened with 'register entry {}'!", entry, e);
			throw new RuntimeException(e);
		} 

	}

	@Override
	public void unregisterService(RegisterEntry entry) throws Exception {
		lock.acquire(3, TimeUnit.SECONDS);
		ServiceInstance<InstanceMetadata> thisInstance;
		try {
			thisInstance = ServiceInstance.<InstanceMetadata> builder()
					.name(entry.getServiceContract()).id(entry.getInstanceMetadata().getId().toString())
					.payload(entry.getInstanceMetadata()).build();
			serviceDiscovery.unregisterService(thisInstance);
		} catch (Exception e) {
			LOGGER.error(
					"Unregister instance error happened with 'register entry {}'!", entry, e);
			throw new RuntimeException(e);
		}
		finally{
			 lock.release();
		}

	}
    /**
     *Notes: All the nodes and their datum will be deleted once the connection is broken(by call  {@code destroy()} or session timeout) between the zookeeper's client and server.
     * 
     */
	@Override
	public void destroy() throws IOException {
		close();
		serializer = null;
		serviceDiscovery = null;
		client = null;
		closeableList.clear();
		providers.clear();
		cache.clear();
	}
	 /**
     * All the nodes and their datum will be deleted once the connection is broken(by call  {@code close()} or session timeout) between the zookeeper's client and server.
     * 
     */
	public void close() throws IOException {
		for (Closeable closeable : closeableList) {
	          CloseableUtils.closeQuietly(closeable);
	    }
		CloseableUtils.closeQuietly(serviceDiscovery);
		CloseableUtils.closeQuietly(cache);
		CloseableUtils.closeQuietly(client);

	}

}
