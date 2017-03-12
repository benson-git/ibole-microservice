package com.github.ibole.microservice.registry.instance.grpc;

import io.grpc.HandlerRegistry;
import io.grpc.ServerMethodDefinition;

/**
 * Extension for HandlerRegistry. gRPC's has not support for service discovery/registry so far. We
 * cannot manipulate the registered services from gRPC once we register our services with the normal
 * way: serverBuilder.addService(your service). The workaround solution is to custom our
 * HandlerRegistry and set it to {@code serverBuilder.fallbackHandlerRegistry(fallbackRegistry)}
 * 
 * @author bwang
 *
 */
public class ExtHandlerRegistry extends HandlerRegistry {

  /*
   * (non-Javadoc)
   * 
   * @see io.grpc.HandlerRegistry#lookupMethod(java.lang.String, java.lang.String)
   */
  @Override
  public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
    // TODO Auto-generated method stub
    return null;
  }

}
