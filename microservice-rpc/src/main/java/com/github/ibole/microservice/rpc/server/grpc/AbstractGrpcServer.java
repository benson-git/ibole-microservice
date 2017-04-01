package com.github.ibole.microservice.rpc.server.grpc;

import com.github.ibole.microservice.registry.service.ServiceDefinitionAdapter;
import com.github.ibole.microservice.registry.service.ServiceDefinitionLoader;
import com.github.ibole.microservice.rpc.server.AbstractRpcServer;
import com.github.ibole.microservice.rpc.server.RpcServerInterceptor;
import com.github.ibole.microservice.rpc.server.exception.RpcServerException;

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
 * <p>Copyright 2016, iBole Inc. All rights reserved.
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
  
  private Server server;
  
  private List<ServerInterceptor> adaptedInterceptors = Lists.newArrayList();

  //private ExecutorService executor; 
  /**
   * Load gRPC specified service definition and then add to gRPC registry.
   * 
   * @param serverBuilder NettyServerBuilder
   * @return the instance of NettyServerBuilder
   */
  @SuppressWarnings("unchecked")
  protected T bindService(T serverBuilder) {
    
    List<ServiceDefinitionAdapter<?>> services = ServiceDefinitionLoader.loader().getServiceList();
    for (ServiceDefinitionAdapter<?> service : services) {
      serverBuilder
          .addService(ServerInterceptors.intercept((ServerServiceDefinition)service.getServiceDefinition(), adaptedInterceptors));
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
      adapteGrpcServerInterceptor();
      server = buildServer();
      server.start();
    } catch (IOException ex) {
      throw new RpcServerException("Rpc start error happened", ex);
    }

  }

  protected abstract Server buildServer() throws IOException;
  
  private void adapteGrpcServerInterceptor(){
    
    List<RpcServerInterceptor> interceptors = getInterceptors(); 
    
    for(RpcServerInterceptor interceptor : interceptors){
      
      if(ServerInterceptor.class.isAssignableFrom(interceptor.getClass())){
        adaptedInterceptors.add((ServerInterceptor) interceptor);
        logger.debug("Found rpc server interceptor '{}'", interceptor.getClass().getName());
      }
      
    }
  }

}
