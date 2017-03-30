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

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.config.rpc.client.ClientOptions;

import io.grpc.NameResolverProvider;

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
public abstract class AbstractNameResolverProvider<N extends AbstractNameResolverProvider<N>>
    extends NameResolverProvider {

  private final ClientOptions callOptions;

  protected abstract N build(ClientOptions callOptions);

  @SuppressWarnings("rawtypes")
  public static AbstractNameResolverProvider provider() {
    return (AbstractNameResolverProvider) providers().get(0);
  }

  public final ClientOptions getCallOptions() {
    return callOptions;
  }

  public final N withRegistryCenterAddress(ServerIdentifier registryCenterAddress) {
    return build(callOptions.withRegistryCenterAddress(registryCenterAddress));
  }

  public final N withServiceEndpoint(String serviceEndpoint) {
    return build(callOptions.withServiceEndpoint(serviceEndpoint));

  }

  public final N withUsedTls(boolean usedTls) {
    return build(callOptions.withUsedTls(usedTls));
  }

  public final N withZoneToPrefer(String zoneToPrefer) {
    return build(callOptions.withZoneToPrefer(zoneToPrefer));
  }

  protected AbstractNameResolverProvider() {
    this.callOptions = ClientOptions.DEFAULT;
  }

  protected AbstractNameResolverProvider(ClientOptions callOptions) {
    this.callOptions = callOptions;
  }

  // @SuppressWarnings("unchecked")
  // private N fromThis() {
  // return (N) this;
  // }
}
