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

package com.github.ibole.microservice.config.rpc.client;

import com.github.ibole.microservice.common.ServerIdentifier;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

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
 * /** The collection of runtime options for a new client channel builder.
 *
 * <p>
 * A field that is not set is {@code null}.
 *
 * @author bwang
 *
 */
public class ClientOptions implements Serializable {

  private static final long serialVersionUID = 1L;

  private ServerIdentifier registryCenterAddress;

  private String zoneToPrefer;

  private String serviceEndpoint;

  private boolean usedTls;
  
  private String serverHostOverride;

  /**
   * A blank {@code ClientOptions} that all fields are not set.
   */
  public static final ClientOptions DEFAULT = new ClientOptions();

  /**
   * Copy constructor.
   */
  private ClientOptions() {
    // do nothing.
  }

  public ClientOptions withUsedTls(boolean usedTls) {
    ClientOptions options = new ClientOptions(this);
    options.usedTls = usedTls;
    return options;
  }

  public ClientOptions withRegistryCenterAddress(ServerIdentifier registryCenterAddress) {
    ClientOptions options = new ClientOptions(this);
    options.registryCenterAddress = registryCenterAddress;
    return options;
  }

  public ClientOptions withZoneToPrefer(String zoneToPrefer) {
    ClientOptions options = new ClientOptions(this);
    this.zoneToPrefer = zoneToPrefer;
    return options;
  }

  public ClientOptions withServiceEndpoint(String serviceEndpoint) {
    this.serviceEndpoint = serviceEndpoint;
    return this;
  }
  
  public ClientOptions withServerHostOverride(String serverHostOverride) {
    this.serverHostOverride = serverHostOverride;
    return this;
  }

  /**
   * Copy constructor.
   */
  private ClientOptions(ClientOptions other) {
    registryCenterAddress = other.registryCenterAddress;
    zoneToPrefer = other.zoneToPrefer;
    serviceEndpoint = other.serviceEndpoint;
    usedTls = other.usedTls;
    serverHostOverride = other.serverHostOverride;
  }
  
  

  /**
   * @return the registryCenterAddress
   */
  public ServerIdentifier getRegistryCenterAddress() {
    return registryCenterAddress;
  }

  /**
   * @return the zoneToPrefer
   */
  public String getZoneToPrefer() {
    return zoneToPrefer;
  }

  /**
   * @return the serviceEndpoint
   */
  public String getServiceEndpoint() {
    return serviceEndpoint;
  }

  /**
   * @return the usedTls
   */
  public boolean isUsedTls() {
    return usedTls;
  }

  /**
   * @return the serverHostOverride
   */
  public String getServerHostOverride() {
    return serverHostOverride;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("registryCenterAddress", registryCenterAddress)
        .add("zoneToPrefer", zoneToPrefer).add("serviceEndpoint", serviceEndpoint)
        .add("usedTls", usedTls) .add("serverHostOverride", serverHostOverride).toString();
  }
}
