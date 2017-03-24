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
import com.github.ibole.microservice.registry.ServiceRegistry;
import com.github.ibole.microservice.registry.ServiceRegistryProvider;
import com.github.ibole.microservice.rpc.client.grpc.ZkNameResolverProvider;

import com.google.common.net.HostAndPort;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.AfterClass;
import org.junit.Before;
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
public class ZkNameResolverTest {

  private URI targetService = URI.create("zk://com.myservice");
  private ServerIdentifier identifier;
  private CuratorFramework client;
  private ServiceRegistry<HostMetadata> serviceRegistry;
  private String rootZnode = "/root/rpc";
  private RegisterEntry entry;
  private String zone = "myzone";
  
  @Before
  public void setup() {
    HostAndPort hostAndPort1 = HostAndPort.fromString("localhost:2181");
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
    
  }
    
  @Test
  public void testServiceRegistry() throws Exception {
    
    entry = new RegisterEntry();
    HostMetadata metadata = new HostMetadata("localhost", 4443, zone, true);
    entry.setServiceName(ServerIdentifier.BASE_KEY_PREFIX);
    entry.setServiceContract(targetService.getAuthority());     
    entry.setDescription(targetService.getAuthority());
    entry.setLastUpdated(Calendar.getInstance().getTime());
    entry.setHostMetadata(metadata);
    serviceRegistry.register(entry);
                      
    List<String> services = client.getChildren().forPath(rootZnode+"/"+targetService.getAuthority());
    services.size();
  }

  @Test
  public void testZk() {
     
    NameResolver zkNameResolver =
        ZkNameResolverProvider.newBuilder().setPreferredZone(zone).setUsedTls(true)
            .setZookeeperAddress(identifier).build().newNameResolver(targetService, Attributes.EMPTY);
    
    zkNameResolver.start(new Listener(){

      @Override
      public void onError(Status arg0) {
        
      }

      @Override
      public void onUpdate(List<ResolvedServerInfoGroup> resolvedServers, Attributes attrs) {
        
        ResolvedServerInfoGroup serverGroup = resolvedServers.get(0);
        ResolvedServerInfo serverInfo = serverGroup.getResolvedServerInfoList().get(0);
        org.junit.Assert.assertEquals("localhost", serverInfo.getAddress().toString());
      }
      
    });

  }
  
  @AfterClass
  public static void destroy(){
    
    AbstractDiscoveryFactory.destroyAll();
  }
  
}
