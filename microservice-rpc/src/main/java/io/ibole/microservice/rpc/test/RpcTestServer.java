package io.ibole.microservice.rpc.test;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.ibole.infrastructure.common.properties.ConfigurationBuilder;
import io.ibole.infrastructure.common.properties.ConfigurationHolder;
import io.ibole.microservice.common.utils.Constants;
import io.ibole.microservice.rpc.server.RpcServerInterceptor;
import io.ibole.microservice.rpc.server.RpcServerInterceptorProvider;
import io.ibole.microservice.rpc.server.ServerBootstrap;
import io.ibole.microservice.rpc.server.grpc.AbstractGrpcServer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * Rpc Server for unit test.
 * @author bwang
 *
 */
public class RpcTestServer extends AbstractGrpcServer {
   
  private Server rpcServer = null;
  
  @Override
  public void configure(int pPort, boolean pUseTls) {
    try {
      // load properties
      ConfigurationBuilder builder = new ConfigurationBuilder().defaults();
      builder.properties(
          ServerBootstrap.class.getResource(Constants.RPC_SERVER_PROPERTY_FILE).toURI().toURL());
      builder.override(System.getProperties());
      Map<String, String> props = builder.build();
      ConfigurationHolder.set(props);
      // end of load properties
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Server buildServer() throws IOException {

    ServerBuilder serverBuilder = ServerBuilder.forPort(0);
    
    serverBuilder = bindService(serverBuilder);
    rpcServer = serverBuilder.build();
    return rpcServer;
  }

  public Server getServer(){
    return rpcServer;
  }

  /**
   * @param rpcServer
   */
  public void registerInterceptors() {
    List<RpcServerInterceptor> interceptors = RpcServerInterceptorProvider.getInterceptors();
    for(RpcServerInterceptor interceptor : interceptors){
       registerInterceptor(interceptor);
    }
  }
}
