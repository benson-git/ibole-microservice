package io.ibole.microservice.rpc.server;

import com.google.common.collect.Lists;

import java.util.List;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * 版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
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
