package io.ibole.microservice.rpc.client.grpc;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;

import java.net.URI;

/*********************************************************************************************.
 * 
 * 
 * <p>版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
 * 
 * <p></p>
 *********************************************************************************************/


public class ZkNameResolverProvider extends NameResolverProvider {

  public static final String SCHEME = "zk";
  
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
      return new ZkNameResolver(targetUri, params, GrpcUtil.TIMER_SERVICE,
          GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    } else {
      return null;
    }
  }

  @Override
  public String getDefaultScheme() {
    
    return SCHEME;
  }

}
