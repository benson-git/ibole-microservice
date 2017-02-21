package io.ibole.microservice.rpc.server.grpc;

import io.ibole.microservice.common.utils.Constants;
import io.ibole.microservice.common.utils.SslUtils;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;

import java.io.IOException;
import java.util.concurrent.Executors;


/**
 * Server that manages startup/shutdown of all services.
 * 
 * @author bwang
 *
 */
public class GrpcServer extends AbstractGrpcServer<NettyServerBuilder> {

  private boolean useTls = Constants.RpcServerEnum.DEFAULT_CONFIG.isUseTls();

  private int port = Constants.RpcServerEnum.DEFAULT_CONFIG.getPort();

  public void configure(int pPort, boolean pUseTls) {
    port = pPort;
    useTls = pUseTls;
  }

  /**
   * Build NettyServerBuilder.
   * 1. Enable tls or not
   * 2. Load gRPC specified service definition and then add to gRPC registry.
   */
  public Server buildServer() throws IOException {

    SslContext sslContext;
    NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port).executor(Executors.newFixedThreadPool(8));
    if (useTls) {
      sslContext = GrpcSslContexts
          .forServer(SslUtils.loadCert("server.pem"), SslUtils.loadCert("server.key")).build();
      serverBuilder.sslContext(sslContext);
    }
    serverBuilder = bindService(serverBuilder);
 
    return serverBuilder.flowControlWindow(1024 * 1024).build();// 1024*1024 = 1MiB
  }
  
}
