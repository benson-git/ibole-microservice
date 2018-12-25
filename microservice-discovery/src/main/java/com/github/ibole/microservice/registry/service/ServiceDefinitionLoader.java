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

import com.github.ibole.microservice.common.exception.ProviderNotFoundException;

import java.util.List;
import java.util.ServiceLoader;

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
public abstract class ServiceDefinitionLoader<S> {

  @SuppressWarnings("rawtypes")
  private static final ServiceDefinitionLoader<ServiceDefinitionAdapter> provider =
      load(Thread.currentThread().getContextClassLoader());

  @SuppressWarnings({"rawtypes", "unchecked"})
  static final ServiceDefinitionLoader<ServiceDefinitionAdapter> load(ClassLoader cl) {
    ServiceLoader<ServiceDefinitionLoader> providers = ServiceLoader.load(ServiceDefinitionLoader.class, cl);
    ServiceDefinitionLoader<ServiceDefinitionAdapter> best = null;

    for (ServiceDefinitionLoader<ServiceDefinitionAdapter> current : providers) {
      if (!current.isAvailable()) {
        continue;
      } else if (best == null) {
        best = current;
      } else if (current.priority() > best.priority()) {
        best = current;
      }
    }
    
    return best;
  }

  /**
   * Whether this provider is available for use, taking the current environment into consideration.
   * If {@code false}, no other methods are safe to be called.
   * @return true if the registry provider is available, otherwise return false
   */
  protected abstract boolean isAvailable();

  /**
   * A priority, from 0 to 10 that this provider should be used, taking the current environment into
   * consideration. 5 should be considered the default, and then tweaked based on environment
   * detection. A priority of 0 does not imply that the provider wouldn't work; just that it should
   * be last in line.
   * @return the priority int
   */
  protected abstract int priority();
  
  /**
   * Locate the instance of provider ServiceDefinitionLoader.
   * 
   * @return the ClassLoader-wide default server.
   * @throws ProviderNotFoundException if no provider is available
   */
  @SuppressWarnings("rawtypes")
  public static ServiceDefinitionLoader loader() {
    if (provider == null) {
      throw new ProviderNotFoundException(
          "No functional server found. " + "Try adding a dependency on the rpc framework artifact");
    }
    return provider;
  } 
  
  public abstract List<S> getServiceList();
}
