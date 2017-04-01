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

package com.github.ibole.microservice.registry.service;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * A service definition adapter to adapt for variant service definition from third party.  
 * @author bwang
 *
 */
public abstract class ServiceDefinitionAdapter<S> {
  
  protected S serviceDefinition;
  
  public ServiceDefinitionAdapter(S serviceDefinition){
    this.serviceDefinition = serviceDefinition;
  }
  
  public S getServiceDefinition(){
    return serviceDefinition;
  }
  
  public abstract String getServiceName();
  
  public abstract String getServiceDescription();
  
  //public abstract String getServiceVersion();

}
