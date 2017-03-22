package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.common.ServerIdentifier;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>.
 * </p>
 *********************************************************************************************/

/**
 * 
 * NOTE: There are two entries to specify the ZkNameResolver to channel builder below, zookeeper name resolver 
 * provider is used for the last case. 
 * 
 * <li>i.  Service locator with ZkNameResolverProvider
 * <li>ii. With the help of NettyChannelBuilder.nameResolverFactory(NameResolver.Factory resolverFactory)
 * 
 * @author bwang
 *
 */
public class ZkNameResolverProvider extends NameResolverProvider {

  public static final String SCHEME = "zk";
  
  private final ServerIdentifier zookeeperAddress;

  private final String zoneToPrefer;
  
  private final boolean usedTls;

  private ZkNameResolverProvider(ServerIdentifier zookeeperAddress, String zoneToPrefer, boolean usedTls) {
    this.zookeeperAddress = zookeeperAddress;
    this.zoneToPrefer = zoneToPrefer;
    this.usedTls = usedTls;
  }


  @Override
  protected boolean isAvailable() {

    return true;
  }

  @Override
  protected int priority() {
   
    return 5;
  }

  @Override
  public NameResolver newNameResolver(URI targetUri, Attributes params) {
    if (SCHEME.equals(targetUri.getScheme())) {
      return new ZkNameResolver(targetUri, params, zookeeperAddress, zoneToPrefer, usedTls);
    } else { 
      return null;
    }
  }

  @Override
  public String getDefaultScheme() { 
    return SCHEME;
  }
  
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private ServerIdentifier zookeeperAddress;
    private String zoneToPrefer;
    private boolean usedTls;

    public Builder setZookeeperAddress(ServerIdentifier zookeeperAddress) {
      this.zookeeperAddress = zookeeperAddress;
      return this;
    }

    public Builder setPreferredZone(String zoneToPrefer) {
      this.zoneToPrefer = zoneToPrefer;
      return this;
    }

    public Builder setUsedTls(boolean usedTls) {
      this.usedTls = usedTls;
      return this;
    }


    public NameResolverProvider build() {
      return new ZkNameResolverProvider(zookeeperAddress, zoneToPrefer, usedTls);
    }
  }

}
