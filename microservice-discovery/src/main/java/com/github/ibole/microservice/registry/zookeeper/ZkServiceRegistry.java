package com.github.ibole.microservice.registry.zookeeper;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.InstanceMetadata;
import com.github.ibole.microservice.discovery.RegisterEntry;
import com.github.ibole.microservice.registry.AbstractServiceRegistry;
import com.github.ibole.microservice.registry.RegistryManagerException;

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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper registry.
 * 
 * @author bwang
 *
 */
public class ZkServiceRegistry extends AbstractServiceRegistry {

  private static final int LOCK_TIME = 3;
  private CuratorFramework client = null;
  private ServiceDiscovery<InstanceMetadata> serviceDiscovery;
  private JsonInstanceSerializer<InstanceMetadata> serializer;
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

        serializer = new JsonInstanceSerializer<InstanceMetadata>(InstanceMetadata.class);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
            .basePath(buildBasePath()).client(client).serializer(serializer).build();
        client.checkExists().creatingParentContainersIfNeeded().forPath(buildBasePath());
        if (client.checkExists().forPath(buildBasePath()) == null) {
          client.create().creatingParentsIfNeeded()/** .withMode(CreateMode.PERSISTENT) */
              .forPath(buildBasePath());
        }
      }
    } catch (Exception e) {
      log.error("Service registry start error for server identifier '{}' !",
          getIdentifier().getConnectionString(), e);
      throw new RegistryManagerException(e);
    }
  }

  private String buildServicePath(RegisterEntry instance) {

    return buildBasePath() + "/" + instance.getServiceContract()
            + "/" + instance.getInstanceMetadata().generateKey();
  }
  
  @Override
  public void register(RegisterEntry instance) {
    
    boolean acquiredLock = false;
    try {
      
      if (client.checkExists().forPath(buildServicePath(instance)) != null) {
        log.info("Service: [{}] already has been registered on {}, skip current registery.",
            instance.getServiceContract(), instance.getInstanceMetadata().toString());
        acquiredLock = false;
        return;
      }
      
      if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {  
        acquiredLock = true;
        ServiceInstance<InstanceMetadata> thisInstance =
            ServiceInstance.<InstanceMetadata>builder().name(instance.getServiceContract())
                .address(instance.getInstanceMetadata().getHostname())
                .port(instance.getInstanceMetadata().getPort())
                .id(instance.getInstanceMetadata().generateKey())
                .payload(instance.getInstanceMetadata()).serviceType(ServiceType.PERMANENT).build();
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
    ServiceInstance<InstanceMetadata> thisInstance;
    try {
      if (lock.acquire(LOCK_TIME, TimeUnit.SECONDS)) {
        thisInstance = ServiceInstance.<InstanceMetadata>builder().name(entry.getServiceContract())
            .id(entry.getInstanceMetadata().generateKey()).payload(entry.getInstanceMetadata())
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
                  .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
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
