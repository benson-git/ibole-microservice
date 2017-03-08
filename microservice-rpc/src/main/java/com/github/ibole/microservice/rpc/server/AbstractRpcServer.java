package com.github.ibole.microservice.rpc.server;

import com.google.common.collect.Lists;

import java.util.List;

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


public abstract class AbstractRpcServer implements RpcServer {


  private List<RpcServerInterceptor> interceptors = Lists.newArrayList();


  public void registerInterceptor(RpcServerInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<RpcServerInterceptor> getInterceptors() {
    return interceptors;
  }

}
