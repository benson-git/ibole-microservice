package com.github.ibole.microservice.discovery.zookeeper;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.AbstractServiceDiscovery;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.ServiceDiscoveryException;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

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
  private JsonInstanceSerializer<HostMetadata> serializer;
  private TreeCache cache = null;

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
      cache = TreeCache.newBuilder(client, buildBasePath()).build();
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
   * 监控指定节点和节点下的所有的节点的变化-无限监听.
   * <pre>Cache synchronization:
   * http://stackoverflow.com/questions/39557653/zookeeper-curator-cache-how-to-wait-for-synchronization
   * <pre>The problem of treecache-eventual-consistency:
   * http://stackoverflow.com/questions/41922928/curator-treecache-eventual-consistency
   * @param serviceName the service name to retrieve the cache
   * @param listener the service state listener
   */
  @Override
  public void watchForCacheUpdates(String serviceName, ServiceStateListener listener) {
    cache.getListenable().addListener(new TreeCacheListener() {
      @Override
      public void childEvent(CuratorFramework client, TreeCacheEvent event)
          throws Exception {
        //Filter the even coming from leases&locks
        if (event.getData() != null && !event.getData().getPath().contains("leases")
            && !event.getData().getPath().contains("locks")) {
          switch (event.getType()) {
            case NODE_ADDED:
              // update the all children - filter the node_added even is for the parent node creation for serviceName
              if (event.getData().getPath().contains(serviceName)) {
                listener.update(getServiceInstancesFromCache(serviceName));
                if (logger.isInfoEnabled()) {
                  logger.info("Service is added at path '{}' in cache", event.getData().getPath());
                }
              }
              break;
            case NODE_REMOVED:
              listener.update(getServiceInstancesFromCache(serviceName));
              if (logger.isInfoEnabled()) {
                logger.info("Service is removed at path '{}' in cache", event.getData().getPath());
              }
              break;
            case NODE_UPDATED:
              listener.update(getServiceInstancesFromCache(serviceName));
              if (logger.isInfoEnabled()) {
                logger.info("Service is updated at path '{}' in cache", event.getData().getPath());
              }
            default:
              break;
          }
        }
      }

    });
    try {
      cache.start();
    } catch (Exception e) {
      logger.error("Watch start error happened for path '{}'!", buildBasePath(), e);
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
    cache = null;
    client = null;
  }

  /**
   * All the nodes and their datum will be deleted once the connection is broken(by call
   * {@code close()} or session timeout) between the zookeeper's client and server.
   * 
   */
  public void close() throws IOException {
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
      } catch (KeeperException.NoNodeException ignore) {
        if (logger.isDebugEnabled()){
           logger.debug("No data found for path '{}'!", znode);
        }
        return null;
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }).filter( data -> data != null && data.length != 0).map(data -> {
      try {
        return serializer.deserialize(data).getPayload();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }).collect(Collectors.toList());
  }
  
  private List<HostMetadata> getServiceInstancesFromCache(String serviceName) {
    Map<String, ChildData> children = cache.getCurrentChildren(ZKPaths.makePath(buildBasePath(), serviceName));
    if(children == null){
      return Lists.newArrayList();
    }
    return children.entrySet().stream().map(entry -> {
      try {
        return entry.getValue();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }).filter( data -> data != null).map(data -> {
      try {
        return serializer.deserialize(data.getData()).getPayload();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }).collect(Collectors.toList());
  }

}
