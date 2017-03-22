package com.github.ibole.microservice.discovery.zookeeper;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.common.utils.Constants;
import com.github.ibole.microservice.discovery.AbstractServiceDiscovery;
import com.github.ibole.microservice.discovery.DiscoveryManagerException;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.ServiceRegistryChangeListener;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import org.apache.zookeeper.Watcher;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Zookeeper discovery.
 * 
 * @author bwang
 *
 */
public class ZkServiceDiscovery extends AbstractServiceDiscovery {

  private CuratorFramework client = null;
  private ServiceDiscovery<HostMetadata> serviceDiscovery;
  private Map<String, ServiceProvider<HostMetadata>> providers = Maps.newHashMap();
  private List<Closeable> closeableList = Lists.newArrayList();
  private JsonInstanceSerializer<HostMetadata> serializer;
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

      serializer = new JsonInstanceSerializer<HostMetadata>(HostMetadata.class);
      serviceDiscovery = ServiceDiscoveryBuilder.builder(HostMetadata.class)
          .basePath(buildBasePath()).client(client).serializer(serializer).build();

      cache = new PathChildrenCache(client, buildBasePath(), true);
      cache.start();

    } catch (Exception e) {
      logger.error("Service registry start error for server identifier '{}' !",
          getIdentifier().getConnectionString(), e);
      throw new DiscoveryManagerException(e);
    }
  }

  @Override
  public List<HostMetadata> listAll(String serviceContract) {
    Collection<ServiceInstance<HostMetadata>> instances = null;
    List<HostMetadata> metadatum = Lists.newArrayList();
    try {
      serviceDiscovery.start();
      instances = serviceDiscovery.queryForInstances(serviceContract);
      for (ServiceInstance<HostMetadata> serviceInstance : instances) {
        metadatum.add(serviceInstance.getPayload());
      }

    } catch (Exception e) {
      logger.error("List all instances error happened with path '{}'!",
          buildBasePath() + Constants.ZK_DELIMETER + serviceContract, e);
      throw new DiscoveryManagerException(e);

    }

    return metadatum;
  }

  /**
   * Get the instance of InstanceMetadata with the Round Robin strategy.
   * 
   * @return the instance of InstanceMetadata.
   */
  @Override
  public HostMetadata getInstance(String serviceContract) {
    ServiceProvider<HostMetadata> provider = providers.get(serviceContract);
    HostMetadata instance = null;
    try {
      if (provider == null) {
        provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceContract)
            .providerStrategy(new RoundRobinStrategy<HostMetadata>()).build();
        provider.start();
        closeableList.add(provider);
        providers.put(serviceContract, provider);
      }
      instance = provider.getInstance().getPayload();

    } catch (Exception e) {
      logger.error("Retrieve instance error happened with with path '{}'!",
          buildBasePath() + Constants.ZK_DELIMETER + serviceContract, e);
      throw new DiscoveryManagerException(e);
    }
    return instance;
  }

  /**
   * Get service instance by specified service contract and id.
   * 
   * @param serviceContract the service contract
   * @param id the node id
   */
  public HostMetadata getInstanceById(String serviceContract, String id) {
    HostMetadata metadata = null;
    try {
      ServiceInstance<HostMetadata> instance =
          serviceDiscovery.queryForInstance(serviceContract, id);
      metadata = instance.getPayload();
    } catch (Exception e) {
      logger.error("Retrieve instance error happened with '{}'!",
          buildBasePath() + Constants.ZK_DELIMETER + serviceContract, e);
      throw new DiscoveryManagerException(e);
    }
    return metadata;
  }
  
  public boolean watchForUpdates(final String serviceName, ServiceStateListener listener){
    String znode = buildBasePath() + Constants.ZK_DELIMETER + serviceName;
    return watchNodeForUpdates(znode, listener);
  }
 
  private boolean watchNodeForUpdates(final String znode, ServiceStateListener listener){
    try {
      client.getChildren().usingWatcher((Watcher) watchedEvent -> {
        try {
          watchNodeForUpdates(znode, listener);
          listener.update(getServicesForNode(watchedEvent.getPath()));
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }).forPath(znode);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private List<HostMetadata> getServicesForNode(String znode) throws Exception {
    List<String> children = client.getChildren().forPath(znode);
    return children.stream().map(child -> {
      try {
        return client.getData().forPath(znode + Constants.ZK_DELIMETER + child);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }).map(data -> {
      try {
        return serializer.deserialize(data).getPayload();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }).collect(Collectors.toList());
  }

  /**
   * Notes: All the nodes and their datum will be deleted once the connection is broken(by call
   * {@code destroy()} or session timeout) between the zookeeper's client and server.
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
   * All the nodes and their datum will be deleted once the connection is broken(by call
   * {@code close()} or session timeout) between the zookeeper's client and server.
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
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
          throws Exception {
        switch (event.getType()) {
          case CHILD_ADDED:
            if (logger.isInfoEnabled()) {
              logger.info("Service is added at path '{}'", event.getData().getPath());
            }
            // listener.nodeAdded(getInfo(event.getData()));
            break;
          case CHILD_REMOVED:
            if (logger.isInfoEnabled()) {
              logger.info("Service is removed at path '{}'", event.getData().getPath());
            }
            // listener.nodeRemoved(getInfo(event.getData()));
            break;
          case CHILD_UPDATED:
            if (logger.isInfoEnabled()) {
              logger.info("Service is updated at path '{}'", event.getData().getPath());
            }
            // listener.nodeUpdated(getInfo(event.getData()));
          default:
            break;
        }

      }

    });
  }

}
