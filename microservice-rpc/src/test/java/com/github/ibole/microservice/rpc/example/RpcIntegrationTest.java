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

package com.github.ibole.microservice.rpc.example;

import com.github.ibole.microservice.discovery.zookeeper.test.AbstractZkServerStarter;
import com.github.ibole.microservice.rpc.example.serviceconsumer.GreeterClient;
import com.github.ibole.microservice.rpc.server.ServerBootstrap;
  

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/service-consumer.xml"})
public class RpcIntegrationTest extends AbstractZkServerStarter{
    @Autowired
    private GreeterClient client;
    
    @Before
    public void setup() {
      String[] args1 = new String[] {"--hostname=localhost", "--port=443", "--reg_servers=localhost:2181", "--use_tls=true"};
      ServerBootstrap.awaitTermination = false;
      ServerBootstrap.main(args1);
    }
    
    @BeforeClass  
    public static void init() throws Exception{
      // start the zk server
      startZKServer();
    }
    
    @AfterClass
    public static void destroy() throws InterruptedException{
      Thread.sleep(2000);
      closeZKServer();
    }
    @Test
    public void test() {
      String response = client.doGreeter();
      System.out.println(response);
      org.junit.Assert.assertTrue(response.equals("Hello world!"));
    }
}