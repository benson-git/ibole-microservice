package com.github.ibole.microservice.registry.zookeeper;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.common.utils.Constants;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.RegisterEntry;
import com.github.ibole.microservice.registry.AbstractServiceRegistry;
import com.github.ibole.microservice.registry.RegistryManagerException;

import com.google.common.base.Throwables;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper registry.
 * 
 * 通过ZooKeeper发布服务，服务启动时将自己的信息注册为临时节点，当服务断掉时ZooKeeper将此临时节点删除，
 * 这样client就不会得到服务的信息了.
 * 
 * @author bwang
 *
 */
public class ZkServiceRegistry extends AbstractServiceRegistry {

  private static final int LOCK_TIME = 3;
  private CuratorFramework client = null;
  private ServiceDiscovery<HostMetadata> serviceDiscovery;
  private JsonInstanceSerializer<HostMetadata> serializer;
  private InterProcessSemaphoreMutex lock;

  public ZkServiceRegistry(ServerIdentifier identifier) {
    super(identifier);
  }
  
  @Override
  public void start() {
    try {
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);  
      client = CuratorFrameworkFactory.builder()  
              .connectString(getIdentifier().getConnectionString())
              .connectionTimeoutMs(10000)
              .retryPolicy(retryPolicy)
              //.namespace("text")
              .build();
      client.start();
      if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
        
        lock = new InterProcessSemaphoreMutex(client, buildBasePath());

        serializer = new JsonInstanceSerializer<HostMetadata>(HostMetadata.class);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(HostMetadata.class)
            .basePath(buildBasePath()).client(client).serializer(serializer).build();
        //force to create a root path if the node is not exist.
        ensureNodeExists(buildBasePath());
      }
    } catch (Exception e) {
      log.error("Service registry start error for server identifier '{}' !",
          getIdentifier().getConnectionString(), e);
      throw new RegistryManagerException(e);
    }
  }

  private String ensureNodeForServiceExists(String serviceName) throws Exception {
    String znode = buildBasePath() + Constants.ZK_DELIMETER + serviceName;
    return ensureNodeExists(znode);
  }

  private String ensureNodeExists(String znode) throws Exception {
    if (client.checkExists().creatingParentContainersIfNeeded().forPath(znode) == null) {
      try {
        client.create().creatingParentsIfNeeded().forPath(znode);
        //ZK制约:临时节点下不能创建子节点
        //client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(znode);
      } catch (KeeperException.NodeExistsException e) {
        // Another Thread/Service/Machine has just created this node for us.
      }
    }
    return znode;
  }

  private String buildServicePath(RegisterEntry instance) {

    return buildBasePath() + Constants.ZK_DELIMETER + instance.getServiceContract()
            + Constants.ZK_DELIMETER + instance.getHostMetadata().generateKey();
  }
  
  @Override
  public void register(RegisterEntry instance) {
    
    boolean acquiredLock = false;
    try {
      
      if (client.checkExists().forPath(buildServicePath(instance)) != null) {
        log.info("Service: [{}] already has been registered on {}, skip current registery.",
            instance.getServiceContract(), instance.getHostMetadata().toString());
        acquiredLock = false;
        return;
      }
      
      if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {  
        acquiredLock = true;
        ServiceInstance<HostMetadata> thisInstance =
            ServiceInstance.<HostMetadata>builder().name(instance.getServiceContract())
                .address(instance.getHostMetadata().getHostname())
                .port(instance.getHostMetadata().getPort())
                .id(instance.getHostMetadata().generateKey())
                .payload(instance.getHostMetadata()).serviceType(ServiceType.DYNAMIC).build();
        serviceDiscovery.start();
        serviceDiscovery.registerService(thisInstance);
        log.info("Registed instance metadata: " + instance.toString());
      }
    } catch (Exception ex) {
      log.error("Register instance {} error happened!", instance.toString(), ex);
      throw new RegistryManagerException(ex);
    } finally {
      try {
        if (acquiredLock) {
          lock.release();
        }
      } catch (Exception ex) {
        log.error("Lock release error happened!", ex);
        throw new RegistryManagerException(ex);
      }
    }

  }

  @Override
  public void unregisterService(RegisterEntry entry) {
    ServiceInstance<HostMetadata> thisInstance;
    try {
      if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
        thisInstance = ServiceInstance.<HostMetadata>builder().name(entry.getServiceContract())
            .id(entry.getHostMetadata().generateKey()).payload(entry.getHostMetadata())
            .build();
        serviceDiscovery.unregisterService(thisInstance);
      }
    } catch (Exception e) {
      log.error("Unregister instance error happened with 'register entry {}'!", entry, e);
      throw new RegistryManagerException(e);
    } finally {
      try {
        lock.release();
      } catch (Exception e) {
        log.error("Lock release error happened!", e);
        throw new RegistryManagerException(e);
      }
    }
  }

  public void unregisterService(final String serviceContractPath) throws Exception {
    List<String> children = client.getChildren().forPath(serviceContractPath);
    children.stream().forEach(child -> {
      try {
        client.delete().forPath(serviceContractPath + Constants.ZK_DELIMETER + children);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    });
  }
  
  public boolean removeServiceRegistry(String serviceContractPath) throws Exception {
    String znode = ensureNodeExists(serviceContractPath);
    try {
      client.delete().guaranteed().deletingChildrenIfNeeded().forPath(znode);
      return true;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Notes: All the nodes and their datum will be deleted once the connection is broken(by call
   * {@code destroy()} or session timeout) between the zookeeper's client and server.
   * 
   */
  @Override
  public void destroy() throws IOException {
    try {
      if (lock != null && lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
        close();
        serializer = null;
        serviceDiscovery = null;
      }
      client = null;
    } catch (Exception e) {
      log.error("Destroy service registry error happened !", e);
      throw new RegistryManagerException(e);
    } finally {
      try {
        lock.release();
      } catch (Exception e) {
        log.error("Lock release error happened!", e);
        throw new RegistryManagerException(e);
      }
    }
  }

  /**
   * All the nodes and their datum will be deleted once the connection is broken(by call
   * {@code close()} or session timeout) between the zookeeper's client and server.
   * 
   * @throws IOException if I/O exception happen
   * 
   */
  public void close() throws IOException {
    try {
      if (lock != null && lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
        CloseableUtils.closeQuietly(serviceDiscovery);
      }
      CloseableUtils.closeQuietly(client);
    } catch (Exception e) {
      log.error("Close service registry error happened !", e);
      throw new RegistryManagerException(e);
    } finally {
      try {
        lock.release();
      } catch (Exception e) {
        log.error("Lock release error happened!", e);
        throw new RegistryManagerException(e);
      }
    }
  }

  /**
   * A class to monitor connection state and re-register to Zookeeper when connection lost.
   *
   */
  public class ZkConnectionStateListener implements ConnectionStateListener {
    private String zkRegPathPrefix;
    private String regContent;

    public ZkConnectionStateListener(String zkRegPathPrefix, String regContent) {
      this.zkRegPathPrefix = zkRegPathPrefix;
      this.regContent = regContent;
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
      if (connectionState == ConnectionState.CONNECTED){
        
      }
      else if (connectionState == ConnectionState.LOST) {
        while (true) {
          try {
            if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
              curatorFramework.create().creatingParentsIfNeeded()
                  .withMode(CreateMode.PERSISTENT)
                  .forPath(zkRegPathPrefix, regContent.getBytes("UTF-8"));
              break;
            }
          } catch (InterruptedException e) {
            // TODO: log something
            break;
          } catch (Exception e) {
            // TODO: log something
          }
        }
      }
    }
  }
}
