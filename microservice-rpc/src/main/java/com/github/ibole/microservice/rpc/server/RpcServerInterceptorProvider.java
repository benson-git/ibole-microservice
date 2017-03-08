package com.github.ibole.microservice.rpc.server;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.ServiceLoader;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


public class RpcServerInterceptorProvider {

  private static List<RpcServerInterceptor> interceptors = load(Thread.currentThread().getContextClassLoader());

  static List<RpcServerInterceptor> load(ClassLoader cl) {
    List<RpcServerInterceptor> interceptors = Lists.newArrayList();
    ServiceLoader<RpcServerInterceptor> providers =
        ServiceLoader.load(RpcServerInterceptor.class, cl);

    for (RpcServerInterceptor current : providers) {
      interceptors.add(current);
    }
    return interceptors;

  }
  
  public static List<RpcServerInterceptor> getInterceptors(){
    return interceptors;
  }
}
