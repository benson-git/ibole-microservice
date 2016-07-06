/**
 * 
 */
package practices.microservice.registry.zookeeper;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.RegisterEntry;
import practices.microservice.registry.AbstractServiceRegistry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author bwang
 *
 */
public class ZkServiceRegistry extends AbstractServiceRegistry {

	private final static int LOCK_TIME = 5;
	private CuratorFramework client = null;
	private ServiceDiscovery<InstanceMetadata> serviceDiscovery;
	private Map<String, ServiceProvider<InstanceMetadata>> providers = Maps.newHashMap();
	private List<Closeable> closeableList = Lists.newArrayList();
	private JsonInstanceSerializer<InstanceMetadata> serializer;
	private InterProcessSemaphoreMutex lock;

	public ZkServiceRegistry(ServerIdentifier identifier) {
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
		lock = new InterProcessSemaphoreMutex(client, buildBasePath());

		serializer = new JsonInstanceSerializer<InstanceMetadata>(InstanceMetadata.class);
		serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
				.basePath(buildBasePath()).client(client).serializer(serializer).build();
		client.checkExists().creatingParentContainersIfNeeded().forPath(buildBasePath());
		// client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(this.getIdentifier().getBaseKey());
		
		} catch (Exception e) {
			LOGGER.error("Service registry start error for server identifier '{}' !", getIdentifier().getConnectionString(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void register(RegisterEntry instance) {
		try {
			if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
				ServiceInstance<InstanceMetadata> thisInstance = ServiceInstance.<InstanceMetadata> builder()
						.name(instance.getServiceContract()).address(instance.getInstanceMetadata().getHostname())
						.port(instance.getInstanceMetadata().getPort())
						.id(instance.getInstanceMetadata().getId().toString()).payload(instance.getInstanceMetadata())
						.serviceType(ServiceType.PERMANENT).build();
				serviceDiscovery.start();
				serviceDiscovery.registerService(thisInstance);
				LOGGER.info("Registed instance metadata: " + instance.getInstanceMetadata().toString());
			}
		} catch (Exception e) {
			LOGGER.error("Register instance {} error happened!", instance.toString(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				LOGGER.error("Lock release error happened!", e);
				throw new RuntimeException(e);
			}
		}

	}

	@Override
	public void unregisterService(RegisterEntry entry) {
		ServiceInstance<InstanceMetadata> thisInstance;
		try {
			if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
				thisInstance = ServiceInstance.<InstanceMetadata> builder().name(entry.getServiceContract())
						.id(entry.getInstanceMetadata().getId().toString()).payload(entry.getInstanceMetadata())
						.build();
				serviceDiscovery.unregisterService(thisInstance);
			}
		} catch (Exception e) {
			LOGGER.error("Unregister instance error happened with 'register entry {}'!", entry, e);
			throw new RuntimeException(e);
		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				LOGGER.error("Lock release error happened!", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Notes: All the nodes and their datum will be deleted once the connection
	 * is broken(by call {@code destroy()} or session timeout) between the
	 * zookeeper's client and server.
	 * 
	 */
	@Override
	public void destroy() throws IOException {
		try {
			if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
				close();
				serializer = null;
				serviceDiscovery = null;
				client = null;
				closeableList.clear();
				providers.clear();
			}
		} catch (Exception e) {
			LOGGER.error("Destroy service registry error happened !", e);
			throw new RuntimeException(e);
		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				LOGGER.error("Lock release error happened!", e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * All the nodes and their datum will be deleted once the connection is
	 * broken(by call {@code close()} or session timeout) between the
	 * zookeeper's client and server.
	 * 
	 */
	public void close() throws IOException {
		try {
			if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
				for (Closeable closeable : closeableList) {
					CloseableUtils.closeQuietly(closeable);
				}
				CloseableUtils.closeQuietly(serviceDiscovery);
				CloseableUtils.closeQuietly(client);
			}
		} catch (Exception e) {
			LOGGER.error("Close service registry error happened !", e);
			throw new RuntimeException(e);
		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				LOGGER.error("Lock release error happened!", e);
				throw new RuntimeException(e);
			}
		}
	}

}
