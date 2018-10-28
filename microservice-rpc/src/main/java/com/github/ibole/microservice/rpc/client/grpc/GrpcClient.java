package com.github.ibole.microservice.rpc.client.grpc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.github.ibole.microservice.common.TLS;
import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.config.rpc.client.RpcClient;
import com.github.ibole.microservice.registry.service.grpc.GrpcConstants;
import com.github.ibole.microservice.rpc.client.exception.RpcClientException;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RPC client helper.
 * 
 * @author bwang
 *
 */
public final class GrpcClient implements RpcClient<AbstractStub<?>> {

  private static Logger log = LoggerFactory.getLogger(GrpcClient.class.getName());
  // Cache the mapping between service type and the method for initializing client stub
  private static final Map<String, Method> STUBS = Maps.newConcurrentMap();
 
  private final AtomicReference<State> state = new AtomicReference<State>(State.LATENT);
  
  private GrpcClientInitializer initializer;
  
  private GrpcClient() {
    // do nothing.
  }

  /**
   * The server identifier is used to connect to registry center.
   */
  @Override
  public void initialize(ClientOptions clientOptions) {
    if (state.compareAndSet(State.LATENT, State.INITIALIZED)
        || state.compareAndSet(State.STOPPED, State.INITIALIZED)) {
       initializer = new GrpcClientInitializer(clientOptions);
       if (log.isInfoEnabled()) {
         log.info("Rpc client is initialized.");
       }
    }
  }

  /**
   * Start RPC client.
   */
  public void start() {
    if (!state.compareAndSet(State.INITIALIZED, State.STARTED)) {
      return;
    }
    //discovery.start();
    if (log.isInfoEnabled()) {
      log.info("Rpc client is started.");
    }
  }

  /**
   * Stop RPC client.
   */
  public void stop() {
    
    if (!state.compareAndSet(State.STARTED, State.STOPPED)) {
      return;
    }
    try {
      STUBS.clear();
      initializer.close();
    } catch (Exception ex) {
      log.error("Rpc client stop error happened", ex);
      throw new RpcClientException(ex);
    }
  
    if (log.isInfoEnabled()) {
      log.info("Rpc client is stopped.");
    }
  }
  
  /**
   * Get remoting service instance for client invocation.
   * 
   * @param type the type of expected service instance
   * @param timeout (millisecond) specify the remoting call will be expired at the specified offset 
   * @return T the instance of T.
   */
  @Override
  public AbstractStub<?> getRemotingService(Class<? extends AbstractStub<?>> type, String preferredZone, TLS usedTls, int timeout) {
    checkArgument(type != null, "The type of service interface cannot be null!");
    checkState(state.get() == State.STARTED, "Grpc client is not started!");
    AbstractStub<?> service;
    Method stubInitializationMethod;
    try {
      //The basic idea of instantiating grpc client stub:
      //  Class stubClazz = ***Stub.class (parameter pass in - Class<? extends AbstractStub<?>> type);
      //  Class grpcClazz = stubClazz.getEnclosingClass();
      //  Field serviceNameFiled = grpcClazz.getDeclaredField("SERVICE_NAME");
      //  String value = (String) serviceNameFiled.get(null);
      
      //E.g. public static final String SERVICE_NAME = "routeguide.RouteGuide";
      String serviceName = type.getEnclosingClass().getDeclaredField(GrpcConstants.SERVICE_NAME).get(null).toString();      
      if (!STUBS.containsKey(type.getName())) {
        // Instantiate the generated gRPC class.
        if (type.getName().endsWith(GrpcConstants.CLIENT_STUB_SUFFIX_BLOCKING)) {
          stubInitializationMethod = type.getEnclosingClass().getMethod(GrpcConstants.NEW_CLIENT_BLOCKING_STUB, Channel.class);
        } else if (type.getName().endsWith(GrpcConstants.CLIENT_STUB_SUFFIX_FUTURE)) {
          stubInitializationMethod = type.getEnclosingClass().getMethod(GrpcConstants.NEW_CLIENT_FUTURE_STUB, Channel.class);
        } else {
          stubInitializationMethod = type.getEnclosingClass().getMethod(GrpcConstants.NEW_CLIENT_ASYN_STUB, Channel.class);
        }
        STUBS.putIfAbsent(type.getName(), stubInitializationMethod);
      } else {
        stubInitializationMethod = STUBS.get(type.getName());
      }
      // instantiate the client stub according to the stub type
      service = (AbstractStub<?>) stubInitializationMethod.invoke(null, initializer.getChannelPool().getChannel(serviceName, preferredZone, usedTls));
      //Customizes the CallOptions passed the deadline to interceptor
      if (timeout > 0) {
         service.withOption(StubDeadlineClientInterceptor.DEADLINE_KEY, Integer.valueOf(timeout));
      }

    } catch (Exception ex) {
      log.error("Get remoting service '{}' error happend", type.getName(), ex);
      throw new RpcClientException(ex);
    }
    return service;
  }

  @Override
  public State getState() {
    
    return this.state.get();
  }

  /**
   * Load the service with lazy load style.
   * 
   * @return the instance of GrpcClient
   */
  public static GrpcClient getInstance() {
    return Loader.INSTANCE;
  }

  private static class Loader {
    private static final GrpcClient INSTANCE = new GrpcClient();

  }

}
