package io.ibole.microservice.rpc.server;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.ServiceLoader;

/*********************************************************************************************.
 * 
 * 
 * <p>版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
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
