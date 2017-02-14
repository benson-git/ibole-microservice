package io.ibole.microservice.rpc.server.grpc;

import io.ibole.microservice.registry.instance.grpc.GrpcServiceDefinitionLoader;
import io.ibole.microservice.rpc.server.AbstractRpcServer;
import io.ibole.microservice.rpc.server.RpcServerInterceptor;
import io.ibole.microservice.rpc.server.exception.RpcServerException;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * The abstract of Server that manages startup/shutdown of all services.
 * @author bwang
 *
 */  
public abstract class AbstractGrpcServer<T extends ServerBuilder<T>> extends AbstractRpcServer {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
 
  private static final int NUM_SERVER_THREADS = 10;
  
  private Server server;
  
  private List<ServerInterceptor> adaptedInterceptors = Lists.newArrayList();

  //private ExecutorService executor; 
  /**
   * Load gRPC specified service definition and then add to gRPC registry.
   * 
   * @param serverBuilder NettyServerBuilder
   * @return the instance of NettyServerBuilder
   */
  protected T bindService(T  serverBuilder) {
    
    List<ServerServiceDefinition> services = GrpcServiceDefinitionLoader.load().getServiceList();
    for (ServerServiceDefinition service : services) {
      serverBuilder
          .addService(ServerInterceptors.intercept(service, adaptedInterceptors));
    }
    return serverBuilder; 
  }

  /**
   * Stop rpc server.
   */
  public void stop() throws InterruptedException {
    server.shutdownNow();
    if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
      logger.error("Timed out waiting for server shutdown");
    }
    //MoreExecutors.shutdownAndAwaitTermination(executor, 5, TimeUnit.SECONDS);
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }


  @Override
  public abstract void configure(int pPort, boolean pUseTls);


  @Override
  public void start() {
    // executor = newFixedThreadPool(NUM_SERVER_THREADS);
    try {
      convertGrpcServerInterceptor();
      server = buildServer();
      server.start();
    } catch (IOException ex) {
      throw new RpcServerException("Rpc start error happened", ex);
    }

  }

  protected abstract Server buildServer() throws IOException;
  
  private void convertGrpcServerInterceptor(){
    
    List<RpcServerInterceptor> interceptors = getInterceptors(); 
    
    for(RpcServerInterceptor interceptor : interceptors){
      
      if(ServerInterceptor.class.isAssignableFrom(interceptor.getClass())){
        adaptedInterceptors.add((ServerInterceptor) interceptor);
        logger.debug("Found rpc server interceptor '{}'", interceptor.getClass().getName());
      }
      
    }
  }

}
