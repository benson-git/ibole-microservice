/**
 * 
 */
package practices.microservice.discovery.zookeeper;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.AbstractServiceDiscovery;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.ServiceRegistryChangeListener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author bwang
 *
 */
public class ZkServiceDiscovery extends AbstractServiceDiscovery {

	private CuratorFramework client = null;
	private ServiceDiscovery<InstanceMetadata> serviceDiscovery;
	private Map<String, ServiceProvider<InstanceMetadata>> providers = Maps.newHashMap();
	private List<Closeable> closeableList = Lists.newArrayList();
	private JsonInstanceSerializer<InstanceMetadata> serializer;
	private PathChildrenCache cache = null;
	
	protected ZkServiceDiscovery(ServerIdentifier identifier) {
		super(identifier);
	}

	@Override
	public void start() {
		try {
		// 1000ms - initial amount of time to wait between retries
		// 3 times - max number of times to retry
		client = CuratorFrameworkFactory.newClient(getIdentifier().getConnectionString(),
				new ExponentialBackoffRetry(1000, 3));
		client.start();
		client.getZookeeperClient().blockUntilConnectedOrTimedOut();

		serializer = new JsonInstanceSerializer<InstanceMetadata>(InstanceMetadata.class);
		serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
				.basePath(buildBasePath()).client(client).serializer(serializer).build();
		// client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(this.getIdentifier().getBaseKey());
		//ServiceCache<InstanceMetadata> serviceCache = serviceDiscovery.serviceCacheBuilder().name("my-service").build();	
//		serviceCache.addListener(new ServiceCacheListener() {
//
//			  @Override
//			  public void stateChanged(CuratorFramework client, ConnectionState newState)
//			  {
//				  LOGGER.debug("State changed");
//			  }
//
//			  @Override
//			  public void cacheChanged()
//			  {
//				  LOGGER.debug("Cache changed");
//			  }
//			}, Executors.newSingleThreadExecutor());
//			serviceCache.start();
		
		cache = new PathChildrenCache(client, buildBasePath(), true);
		cache.start();
		
		} catch (Exception e) {
			LOGGER.error("Service registry start error for server identifier '{}' !", getIdentifier().getConnectionString(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<InstanceMetadata> listAll(String serviceContract) {
		Collection<ServiceInstance<InstanceMetadata>> instances = null;
		List<InstanceMetadata> metadatum = Lists.newArrayList();
		try {
			serviceDiscovery.start();
			instances = serviceDiscovery.queryForInstances(serviceContract);
			for (ServiceInstance<InstanceMetadata> serviceInstance : instances) {
				metadatum.add(serviceInstance.getPayload());
			}

		} catch (Exception e) {
			LOGGER.error("List all instances error happened with path '{}'!",
					buildBasePath() + "/" + serviceContract, e);
			throw new RuntimeException(e);

		}

		return metadatum;
	}

	/**
	 * Get the instance of InstanceMetadata with the Round Robin strategy.
	 * 
	 * @return the instance of InstanceMetadata.
	 */
	@Override
	public InstanceMetadata getInstance(String serviceContract) {
		ServiceProvider<InstanceMetadata> provider = providers.get(serviceContract);
		InstanceMetadata instance = null;
		try {
			if (provider == null) {
				provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceContract)
						.providerStrategy(new RoundRobinStrategy<InstanceMetadata>()).build();
				provider.start();
				closeableList.add(provider);
				providers.put(serviceContract, provider);
			}
			instance = provider.getInstance().getPayload();

		} catch (Exception e) {
			LOGGER.error("Retrieve instance error happened with with path '{}'!",
					buildBasePath() + "/" + serviceContract, e);
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
			LOGGER.error("Retrieve instance error happened with '{}'!",
					buildBasePath() + "/" + serviceContract, e);
			throw new RuntimeException(e);
		}
		return metadata;
	}

	/**
	 * Notes: All the nodes and their datum will be deleted once the connection
	 * is broken(by call {@code destroy()} or session timeout) between the
	 * zookeeper's client and server.
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
	 * All the nodes and their datum will be deleted once the connection is
	 * broken(by call {@code close()} or session timeout) between the
	 * zookeeper's client and server.
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

	@Override
	public void addListener(ServiceRegistryChangeListener listener) {
		cache.getListenable().addListener(new PathChildrenCacheListener() {

	            @Override
	            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
	                switch (event.getType()) {
	                case CHILD_ADDED:
	                	if(LOGGER.isInfoEnabled()){
	                		LOGGER.info("Service is added at path '{}'", event.getData().getPath());
	                	}
	                    //listener.nodeAdded(getInfo(event.getData()));
	                    break;
	                case CHILD_REMOVED:
	                	if(LOGGER.isInfoEnabled()){
	                		LOGGER.info("Service is removed at path '{}'", event.getData().getPath());
	                	}
	                    //listener.nodeRemoved(getInfo(event.getData()));
	                    break;
	                case CHILD_UPDATED:
	                	if(LOGGER.isInfoEnabled()){
	                		LOGGER.info("Service is updated at path '{}'", event.getData().getPath());
	                	}
	                	//listener.nodeUpdated(getInfo(event.getData()));
	                default:
	                    break;
	                }

	            }

	        });
	    }	

}
