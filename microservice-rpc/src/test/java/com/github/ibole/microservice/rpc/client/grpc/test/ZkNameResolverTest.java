/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ibole.microservice.rpc.client.grpc.test;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.AbstractDiscoveryFactory;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.RegisterEntry;
import com.github.ibole.microservice.discovery.zookeeper.test.AbstractZkServerStarter;
import com.github.ibole.microservice.registry.ServiceRegistry;
import com.github.ibole.microservice.registry.ServiceRegistryProvider;
import com.github.ibole.microservice.rpc.client.grpc.ZkNameResolverProvider;

import com.google.common.net.HostAndPort;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolver.Listener;
import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;
import io.grpc.Status;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
@RunWith(JUnit4.class)
public class ZkNameResolverTest extends AbstractZkServerStarter {

  private static URI targetService = URI.create("zk://com.myservice");
  private static ServerIdentifier identifier;
  private static CuratorFramework client;
  private static ServiceRegistry<HostMetadata> serviceRegistry;
  private static String rootZnode = "/root/rpc";
  private static String zone = "myzone";
  private static RegisterEntry entry;
  
  @BeforeClass
  public static void setup() {
    // start the zk server
    initialize();
    
    HostAndPort hostAndPort1 = HostAndPort.fromString("localhost:"+PORT);
    ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    list.add(hostAndPort1);
    identifier = new ServerIdentifier(rootZnode, list);
    serviceRegistry = ServiceRegistryProvider.provider().getRegistryFactory().getServiceRegistry(identifier);
    serviceRegistry.start();
    
    // 1.Connect to zk
    client =
        CuratorFrameworkFactory.newClient(identifier.getConnectionString(), new RetryNTimes(10,
            5000));
    client.start();  
    
    entry = new RegisterEntry();
    HostMetadata metadata = new HostMetadata("localhost", 4443, zone, true);
    entry.setServiceName(ServerIdentifier.BASE_KEY_PREFIX);
    entry.setServiceContract(targetService.getAuthority());     
    entry.setDescription(targetService.getAuthority());
    entry.setLastUpdated(Calendar.getInstance().getTime());
    entry.setHostMetadata(metadata);
    
  }
  
  @Before
  public void registerService(){
   
    serviceRegistry.register(entry);
  }

  @Test
  public void testNameResolver() throws Exception {
    
    NameResolver zkNameResolver =
        ZkNameResolverProvider.newBuilder().setPreferredZone(zone).setUsedTls(true)
            .setZookeeperAddress(identifier).build().newNameResolver(targetService, Attributes.EMPTY);
    
    final CountDownLatch updateLatch = new CountDownLatch(2);
    
    try {

      zkNameResolver.start(new Listener() {
        @Override
        public void onError(Status arg0) {
          updateLatch.countDown();
        }

        @Override
        public void onUpdate(List<ResolvedServerInfoGroup> resolvedServers, Attributes attrs) {

          if (resolvedServers.size() > 0) {
            ResolvedServerInfoGroup serverGroup = resolvedServers.get(0);
            ResolvedServerInfo serverInfo = serverGroup.getResolvedServerInfoList().get(0);
            org.junit.Assert.assertTrue(serverInfo.getAddress().toString().contains("127.0.0.1"));
          }
          updateLatch.countDown();
        }
      });
      
      updateLatch.await();
      Thread.currentThread().sleep(2000);

    } catch (Exception e) {
      updateLatch.countDown();
      throw new RuntimeException(e);
    }
        
  }

  @After
  public void deleteService() throws Exception {
    // MoreExecutors.
    
    client.delete().deletingChildrenIfNeeded()
        .forPath(rootZnode + "/" + targetService.getAuthority());
  }
    
  @AfterClass
  public static void destroy() throws Exception{
    AbstractDiscoveryFactory.destroyAll();
  }
  
}
