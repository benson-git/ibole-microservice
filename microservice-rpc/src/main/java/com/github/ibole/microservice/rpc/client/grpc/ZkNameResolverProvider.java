package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.config.rpc.client.ClientOptions;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.grpclb.GrpclbConstants;

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
public class ZkNameResolverProvider extends AbstractNameResolverProvider<ZkNameResolverProvider> {

  public static final String SCHEME = "zk";
  
  private ZkNameResolverProvider(ClientOptions callOptions) {
    super(callOptions);
  }
  
  public ZkNameResolverProvider() {
    super();
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
      //TODO: don't override the pass in params
      params = Attributes.newBuilder()
                .set(GrpclbConstants.ATTR_LB_POLICY, GrpclbConstants.LbPolicy.ROUND_ROBIN).build();
      return new ZkNameResolver(targetUri, params, getCallOptions());
    } else {
      return null;
    }
  }

  @Override
  public String getDefaultScheme() {
    return SCHEME;
  }

  /* 
   * @see com.github.ibole.microservice.rpc.client.grpc.AbstractNameResolverProvider#build(com.github.ibole.microservice.rpc.client.grpc.ClientOptions)
   */
  @Override
  protected ZkNameResolverProvider build(ClientOptions callOptions) {
    return new ZkNameResolverProvider(callOptions);
  }

}
