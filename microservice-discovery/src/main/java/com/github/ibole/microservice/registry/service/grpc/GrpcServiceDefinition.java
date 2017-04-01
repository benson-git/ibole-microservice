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

package com.github.ibole.microservice.registry.service.grpc;

import com.github.ibole.microservice.registry.service.ServiceDefinitionAdapter;

import io.grpc.ServerServiceDefinition;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 * @param <S>
 *
 */
public class GrpcServiceDefinition extends ServiceDefinitionAdapter<ServerServiceDefinition> {
  

  /**
   * @param serviceDefinition the grpc service definition {@code ServerServiceDefinition}
   */
  public GrpcServiceDefinition(ServerServiceDefinition serviceDefinition) {
    super(serviceDefinition);
  }

  @Override
  public String getServiceName() {
    
    return getServiceDefinition().getServiceDescriptor().getName();
  }

  @Override
  public String getServiceDescription() {
   
    return getServiceDefinition().getServiceDescriptor().getSchemaDescriptor().toString();
  }

}
