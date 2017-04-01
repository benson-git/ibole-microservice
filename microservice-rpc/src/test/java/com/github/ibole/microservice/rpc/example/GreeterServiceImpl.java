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

import com.github.ibole.microservice.registry.service.ServiceExporter;
import com.github.ibole.microservice.rpc.example.GreeterGrpc.GreeterImplBase;
import com.github.ibole.microservice.rpc.example.HelloWorldProto.HelloReply;
import com.github.ibole.microservice.rpc.example.HelloWorldProto.HelloRequest;

import org.springframework.stereotype.Service;

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
@Service
@ServiceExporter
public class GreeterServiceImpl extends GreeterImplBase {
  
  /**
   * <pre>
   * Sends a greeting
   * </pre>
   */
  public void sayHello(HelloRequest request,
      io.grpc.stub.StreamObserver<HelloReply> responseObserver) {
    HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }

}
