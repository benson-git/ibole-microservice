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

package com.github.ibole.microservice.discovery.zookeeper.test;

import com.beust.jcommander.internal.Lists;
import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.registry.ServiceRegistry;
import com.github.ibole.microservice.registry.ServiceRegistryProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
public class ServiceRegistryTest {
  @Test
  public void testServiceRegistryProvider() {
    ServerIdentifier serverIdentifier = new ServerIdentifier("/root", Lists.newArrayList());
    ServiceRegistry<HostMetadata> serviceRegistry = ServiceRegistryProvider.provider().getRegistryFactory().getServiceRegistry(serverIdentifier);
    org.junit.Assert.assertEquals("/root", serviceRegistry.getIdentifier().getRootPath().getPath());
  }
}
