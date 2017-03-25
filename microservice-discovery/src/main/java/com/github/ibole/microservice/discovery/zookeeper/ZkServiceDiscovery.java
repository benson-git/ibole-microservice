package com.github.ibole.microservice.discovery.zookeeper;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.AbstractServiceDiscovery;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.ServiceDiscoveryException;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.io.Closeable;
import java.io.IOException;
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

    } catch (Exception e) {
      logger.error("Service registry start error for server identifier '{}' !",
          getIdentifier().getConnectionString(), e);
      throw new ServiceDiscoveryException(e);
    }
  }
  
  @Override
  public List<HostMetadata> getInstanceList(String serviceContract) {
    List<HostMetadata> metadatum = Lists.newArrayList();
    try {
      metadatum = getServiceInstancesForNode(ZKPaths.makePath(buildBasePath(), serviceContract));
    } catch (Exception e) {
      logger.error("List all instances error happened with path '{}'!",
          ZKPaths.makePath(buildBasePath(), serviceContract), e);
      throw new ServiceDiscoveryException(e);
    }

    return metadatum;
  }

  /**
   * Get service instance by specified service contract and id.
   * 
   * @param serviceContract the service contract
   * @param id the children node id
   */
  @Override
  public HostMetadata getInstanceById(String serviceContract, String id) {
    try {
      byte[] data =
          client.getData().forPath(ZKPaths.makePath(buildBasePath(), serviceContract, id));
      return serializer.deserialize(data).getPayload();
    } catch (KeeperException.NoNodeException ignore) {
      // ignore
    } catch (Exception e) {
      logger.error("Get instance error happened with path '{}'!",
          ZKPaths.makePath(buildBasePath(), serviceContract, id), e);
    }
    return null;
  }
  
  /**
   * Watch the change even on the specified service node and attach a listener on this node.
   * 
   * @param serviceName the servie name
   * @param listener the service state listener
   * @return true if the watch is success, otherwise return false
   */
  @Override
  public boolean watchForUpdates(final String serviceName, ServiceStateListener listener){
    String znode = ZKPaths.makePath(buildBasePath(), serviceName);
    return watchNodeForUpdates(znode, listener);
  }

  /**
   * Path Cache：监视一个路径下1）孩子结点的创建、2）删除，3）以及结点数据的更新. 
   * 产生的事件会传递给注册的PathChildrenCacheListener
   * 能监听所有的子节点且是无限监听的模式 但是指定目录下节点的子节点不再监听 
   * @param serviceName the servie name
   * @param listener the service state listener
   */
  @Override
  public void watchForCacheUpdates(final String serviceName, ServiceStateListener listener) {
    cache = new PathChildrenCache(client, ZKPaths.makePath(buildBasePath(), serviceName), true);
    cache.getListenable().addListener(new PathChildrenCacheListener() {
      @Override
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
          throws Exception {
        switch (event.getType()) {
          case CHILD_ADDED:
            //update the all children
            listener.update(getServiceInstancesForNode(ZKPaths.makePath(buildBasePath(), serviceName)));
            if (logger.isInfoEnabled()) {
              logger.info("Service is added at path '{}'", event.getData().getPath());
            }
            break;
          case CHILD_REMOVED:
            listener.update(getServiceInstancesForNode(ZKPaths.makePath(buildBasePath(), serviceName)));
            if (logger.isInfoEnabled()) {
              logger.info("Service is removed at path '{}'", event.getData().getPath());
            }
            break;
          case CHILD_UPDATED:
            listener.update(getServiceInstancesForNode(ZKPaths.makePath(buildBasePath(), serviceName)));
            if (logger.isInfoEnabled()) {
              logger.info("Service is updated at path '{}'", event.getData().getPath());
            }
          default:
            break;
        }
      }

    });

    try {
      cache.start(StartMode.POST_INITIALIZED_EVENT);
    } catch (Exception e) {
      logger.error("Watch for cache updates error happened with path '{}'!",
          ZKPaths.makePath(buildBasePath(), serviceName), e);
      throw new ServiceDiscoveryException(e);
    }
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
    CloseableUtils.closeQuietly(cache);
    CloseableUtils.closeQuietly(client);

  }
  
  private boolean watchNodeForUpdates(final String znode, ServiceStateListener listener){
    try {
      client.getChildren().usingWatcher((Watcher) watchedEvent -> {
        try {
          watchNodeForUpdates(znode, listener);
          listener.update(getServiceInstancesForNode(watchedEvent.getPath()));
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }).forPath(znode);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private List<HostMetadata> getServiceInstancesForNode(String znode) throws Exception {
    List<String> children = client.getChildren().forPath(znode);
    return children.stream().map(child -> {
      try {
        return client.getData().forPath(ZKPaths.makePath(znode, child));
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

}
