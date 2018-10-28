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

package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.common.TLS;
import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.discovery.zookeeper.test.AbstractZkServerStarter;
import com.github.ibole.microservice.rpc.client.grpc.ChannelPool.InstrumentedChannel;

import com.google.common.net.HostAndPort;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;

import io.grpc.ManagedChannel;

import java.io.IOException;
import java.util.ArrayList;

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
public class GrpcClientInitializerTest extends AbstractZkServerStarter{
  
  static GrpcClientInitializer initializer;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }
  
  @BeforeClass
  public static void init() throws Exception {
    // start the zk server
    startZKServer();
  }

  @Test
  public void test() throws IOException{
    
    HostAndPort hostAndPort1 = HostAndPort.fromString("localhost:"+PORT);
    ArrayList<HostAndPort> list = new ArrayList<HostAndPort>();
    list.add(hostAndPort1);
    String rootZnode = "test";
    ServerIdentifier identifier = new ServerIdentifier(rootZnode, list);
    
    ClientOptions clientOptions = ClientOptions.DEFAULT;
    clientOptions = clientOptions.withRegistryCenterAddress(identifier);
    initializer = new GrpcClientInitializer(clientOptions, null, 1, 1);
    ManagedChannel channel = initializer.getChannelPool().getChannel("myService", "myzone", TLS.ON);
    org.springframework.util.Assert.isInstanceOf(InstrumentedChannel.class, channel);
  }
  
  @AfterClass
  public static void destroy() throws IOException {
    initializer.close();
    closeZKServer();
  }
}
