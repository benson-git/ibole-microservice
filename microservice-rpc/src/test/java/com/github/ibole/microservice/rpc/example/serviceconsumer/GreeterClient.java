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

package com.github.ibole.microservice.rpc.example.serviceconsumer;

import com.github.ibole.microservice.common.TLS;
import com.github.ibole.microservice.config.annotation.Reference;
import com.github.ibole.microservice.rpc.example.serviceprovider.GreeterGrpc.GreeterBlockingStub;
import com.github.ibole.microservice.rpc.example.serviceprovider.HelloWorldProto.HelloReply;
import com.github.ibole.microservice.rpc.example.serviceprovider.HelloWorldProto.HelloRequest;

import org.springframework.stereotype.Component;


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
@Component
public class GreeterClient {

  @Reference(timeout = 3000, preferredZone="myzone", usedTls=TLS.ON)
  private GreeterBlockingStub blockingStub;
  
  public String doGreeter(){
    HelloRequest request = HelloRequest.newBuilder().setName("world!").build();
    HelloReply response = blockingStub.sayHello(request);
    return response.getMessage();
  }
}
