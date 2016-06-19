/**
 * 
 */
package practices.microservice.registry.zookeeper;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.registry.AbstractServiceRegistry;
import practices.microservice.registry.InstanceMetadata;
import practices.microservice.registry.RegisterEntry;
import practices.microservice.registry.RegisterEntry.ServiceType;

import com.google.common.collect.Lists;

/**
 * @author bwang
 *
 */
public class ZkServiceRegistry extends AbstractServiceRegistry<InstanceMetadata> {

	CuratorFramework client = null;

	public ZkServiceRegistry(ServerIdentifier identifier) {
		super(identifier);
	}

	@Override
	public void start() throws IOException {

		client = CuratorFrameworkFactory.newClient(getIdentifier()
				.getConnectionString(), new ExponentialBackoffRetry(1000, 3));
		client.start();
	}
	
	private String getBasePath(ServiceType type, String serviceName){
		checkNotNull(type, "Property 'serviceType' cannot be null!");
		checkNotNull(serviceName, "Property 'serviceName' cannot be null!");
		StringBuilder builder = new StringBuilder();
		builder.append(type).append(serviceName);
		return builder.toString();
	}

	private ServiceDiscoveryBuilder<InstanceMetadata> getDiscoveryBuilder(ServiceType type, String serviceName) {
		String basePath = getBasePath(type, serviceName);
		JsonInstanceSerializer<InstanceMetadata> serializer = null;
		try {
			client.checkExists().creatingParentContainersIfNeeded().forPath(basePath);
			//client.create().creatingParentsIfNeeded().forPath(path);
			serializer = new JsonInstanceSerializer<InstanceMetadata>(InstanceMetadata.class);
		} catch (Exception e) {
			LOGGER.error("Check base path error happened", e);
			throw new RuntimeException(e);
		}
		
		return  ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
				.basePath(basePath).client(client)
				.serializer(serializer);
	}

	@Override
	public void register(RegisterEntry instance) {
		ServiceDiscovery<InstanceMetadata> serviceDiscovery = null;
        try {
			ServiceInstance<InstanceMetadata> thisInstance = ServiceInstance.<InstanceMetadata>builder()
			    .name(instance.getServiceContract())
			    .id(instance.getInstanceMetadata().getId().toString())
			    .payload(instance.getInstanceMetadata())
			    .build();
			
			serviceDiscovery = getDiscoveryBuilder(instance.getServiceType(), instance.getServiceName()).build();
			serviceDiscovery.registerService(thisInstance);
			serviceDiscovery.start();
			
		} catch (Exception e) {
			LOGGER.error("Register instance {} error happened!", e, instance.toString());
			throw new RuntimeException(e);
		} finally{
			CloseableUtils.closeQuietly(serviceDiscovery);
		}

	}

	@Override
	public List<InstanceMetadata> listAll(ServiceType type, String serviceName, String serviceContract) {
		ServiceDiscovery<InstanceMetadata> serviceDiscovery = getDiscoveryBuilder(type, serviceName).build();
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
					"List all instances error happened with 'service type: {}', 'service Name: {}' and 'service contract: {}'!",
					e, type, serviceName, serviceContract);
			throw new RuntimeException(e);
			
		} finally {
			CloseableUtils.closeQuietly(serviceDiscovery);
		}
			
		return metadatum;
	}
    /**
     * Get the instance of InstanceMetadata with the Round Robin strategy.
     * @return the instance of InstanceMetadata.
     */
	@Override
	public InstanceMetadata getInstance(ServiceType type, String serviceName, String serviceContract) {
		ServiceDiscovery<InstanceMetadata> serviceDiscovery = getDiscoveryBuilder(type, serviceName).build();
		ServiceProvider<InstanceMetadata> provider = null;
		InstanceMetadata instance = null;
        try {
    		provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceContract).providerStrategy(new RoundRobinStrategy<InstanceMetadata>()).build();
        	provider.start();
	        instance = provider.getInstance().getPayload();
		} catch (Exception e) {
			LOGGER.error("Retrieve instance error happened with 'service type: {}', 'service Name: {}' and 'service contract: {}'!",
			e, type, serviceName, serviceContract);
			throw new RuntimeException(e);
		} finally {
			CloseableUtils.closeQuietly(provider);
			CloseableUtils.closeQuietly(serviceDiscovery);
		}
		return instance;
	}

	public InstanceMetadata getInstanceById(ServiceType type, String serviceName, String serviceContract, String id) {
		ServiceDiscovery<InstanceMetadata> serviceDiscovery = getDiscoveryBuilder(type, serviceName).build();
		InstanceMetadata metadata = null;
        try {
        	ServiceInstance<InstanceMetadata> instance = serviceDiscovery.queryForInstance(serviceContract, id);
        	metadata = instance.getPayload();
		} catch (Exception e) {
			LOGGER.error("Retrieve instance error happened with 'service type: {}', 'service Name: {}' and 'service contract: {}'!",
			e, type, serviceName, serviceContract);
			throw new RuntimeException(e);
		} finally {
			CloseableUtils.closeQuietly(serviceDiscovery);
		}
		return metadata;
	}
	
	@Override
	public void updateService(RegisterEntry entry) {
		ServiceDiscovery<InstanceMetadata> serviceDiscovery = getDiscoveryBuilder(
				entry.getServiceType(), entry.getServiceName()).build();
		ServiceInstance<InstanceMetadata> thisInstance;
		try {
			thisInstance = ServiceInstance.<InstanceMetadata> builder()
					.name(entry.getServiceContract()).id(entry.getInstanceMetadata().getId().toString())
					.payload(entry.getInstanceMetadata()).build();
			serviceDiscovery.updateService(thisInstance);
		} catch (Exception e) {
			LOGGER.error(
					"Update instance error happened with 'register entry {}'!",
					e, entry);
			throw new RuntimeException(e);
		} finally {
			CloseableUtils.closeQuietly(serviceDiscovery);
		}

	}

	@Override
	public void unregisterService(RegisterEntry entry) {
		ServiceDiscovery<InstanceMetadata> serviceDiscovery = getDiscoveryBuilder(
				entry.getServiceType(), entry.getServiceName()).build();
		ServiceInstance<InstanceMetadata> thisInstance;
		try {
			thisInstance = ServiceInstance.<InstanceMetadata> builder()
					.name(entry.getServiceContract()).id(entry.getInstanceMetadata().getId().toString())
					.payload(entry.getInstanceMetadata()).build();
			serviceDiscovery.unregisterService(thisInstance);
		} catch (Exception e) {
			LOGGER.error(
					"Unregister instance error happened with 'register entry {}'!", e, entry);
			throw new RuntimeException(e);
		} finally {
			CloseableUtils.closeQuietly(serviceDiscovery);
		}


	}

	@Override
	public void destroy() {
		

	}

	@Override
	public void close() throws IOException {
		CloseableUtils.closeQuietly(client);

	}

}
