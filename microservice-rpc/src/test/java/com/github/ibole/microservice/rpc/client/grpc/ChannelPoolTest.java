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

package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.rpc.client.grpc.ChannelPool.InstrumentedChannel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.grpc.ManagedChannel;

import java.io.IOException;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
@RunWith(JUnit4.class)
public class ChannelPoolTest {

  @Mock
  private InstrumentedChannel channel;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test() throws IOException, InterruptedException {

    //when(channel.shutdownNow()).thenReturn(null);

     ClientOptions clientOptions = ClientOptions.DEFAULT;
     // build service endpoint with the default scheme and the service name provided
     String serviceEndpoint =
         AbstractNameResolverProvider.provider().getDefaultScheme() + "://mytesst1";
     clientOptions = clientOptions.withServiceEndpoint(serviceEndpoint);
    // when(channel.)

    ChannelPool pool = 
        ChannelPool.newBuilder().withInitialCapacity(1).withMaximumSize(2)
            .withChannelFactory(new ChannelPool.ChannelFactory() {
              @Override
              public ManagedChannel create(String serviceName, String preferredZone, boolean usedTls) throws IOException {
                return channel;
            }}).build();

    ManagedChannel mychannel1 = pool.getChannel("mytesst1", "myzone", true);
    ManagedChannel mychannel2 = pool.getChannel("mytesst2", "myzone", true);
    ManagedChannel mychannel3 = pool.getChannel("mytesst3", "myzone", true);
    Thread.sleep(1000);
    //System.out.println(pool.size());
    org.junit.Assert.assertTrue(pool.size() == 2);
  }
}
