/*
 * Copyright 2016-2017 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.ibole.microservice.discovery.zookeeper.test;

import com.beust.jcommander.internal.Lists;
import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.RegisterEntry;
import com.github.ibole.microservice.registry.AbstractRegistryFactory;
import com.github.ibole.microservice.registry.ServiceRegistry;
import com.github.ibole.microservice.registry.ServiceRegistryProvider;

import com.google.common.net.HostAndPort;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
@RunWith(JUnit4.class)
public class ServiceRegistryTest extends AbstractZkServerStarter {

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

    HostAndPort hostAndPort1 = HostAndPort.fromString("localhost:" + PORT);
    ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    list.add(hostAndPort1);
    identifier = new ServerIdentifier(rootZnode, list);
    serviceRegistry =
        ServiceRegistryProvider.provider().getRegistryFactory().getServiceRegistry(identifier);
    serviceRegistry.start();

    client =
        CuratorFrameworkFactory.newClient(identifier.getConnectionString(), new RetryNTimes(10,
            5000));
    client.start();

    entry = new RegisterEntry();
    HostMetadata metadata = new HostMetadata("localhost", 4442, zone, true);
    entry.setServiceName(targetService.getAuthority());
    entry.setDescription(targetService.getAuthority());
    entry.setLastUpdated(Calendar.getInstance().getTime());
    entry.setHostMetadata(metadata);

  }

  private void registerService() {
    serviceRegistry.register(entry);
  }

  @Test
  public void testRegisterService() throws Exception {
    registerService();
    org.junit.Assert.assertNotNull("fail to register service",
        client.checkExists().forPath(rootZnode + "/" + targetService.getAuthority()));
  }

  @Test
  public void testUnregisterService() throws Exception {
    registerService();
    serviceRegistry.unregisterService(targetService.getAuthority());
    org.junit.Assert.assertNull("fail to unRegister service",
        client.checkExists().forPath(rootZnode + "/" + targetService.getAuthority()));

  }

  @Test
  public void testServiceRegistryProvider() {
    ServerIdentifier serverIdentifier = new ServerIdentifier("/root", Lists.newArrayList());
    ServiceRegistry<HostMetadata> serviceRegistry =
        ServiceRegistryProvider.provider().getRegistryFactory()
            .getServiceRegistry(serverIdentifier);
    org.junit.Assert.assertEquals("/root", serviceRegistry.getIdentifier().getRootPath().getPath());
  }

  @After
  public void deleteService() throws Exception {
    if (client.checkExists().forPath(rootZnode + "/" + targetService.getAuthority()) != null) {
      client.delete().deletingChildrenIfNeeded()
          .forPath(rootZnode + "/" + targetService.getAuthority());
    }
  }


  @AfterClass
  public static void destroy() {

    AbstractRegistryFactory.destroyAll();
  }

}
