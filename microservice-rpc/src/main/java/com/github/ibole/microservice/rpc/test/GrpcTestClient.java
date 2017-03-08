package com.github.ibole.microservice.rpc.test;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.github.ibole.microservice.rpc.client.exception.RpcClientException;
import com.github.ibole.microservice.rpc.client.grpc.HeaderGrpcClientInterceptor;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

/**
 * RPC client helper.
 * 
 * @author bwang
 *
 */
public class GrpcTestClient  {
  
  private ManagedChannel channel = null;
  private long defaultTimeout = 3000L;
  /**
   * The server identifier is used to connect to registry center.
   */
  public void initialize(int port) {

    channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext(true)
        .intercept(new HeaderGrpcClientInterceptor()).build();
  }

  /**
   * Stop RPC client.
   */
  public void shutdown() {
    try {
      channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
    } catch (Exception ex) {
      throw new RpcClientException(ex);
    }
  }
  
  public ManagedChannel getChannel(){
       return channel;
  }

  /**
   * Get remoting service instance for client invocation.
   * 
   * @param type the type of expected service instance
   * @return T the instance of AbstractStub<T>.
   */
  public <T extends AbstractStub<T>> T getRemotingService(Class<T> type) {
    checkArgument(type != null, "Param cannot be null!");
    checkState(channel != null, "Channel has not been initialized.");
    T service = null;
    try {
      Constructor<T> constructor = type.getDeclaredConstructor(Channel.class);
      constructor.setAccessible(true);
      service = constructor.newInstance(getChannel()).withDeadlineAfter(defaultTimeout, TimeUnit.MILLISECONDS);     
    } catch (Exception ex) {
      throw new RpcClientException("Get remoting service '" + type.getName() + "' error happend", ex);
    }
    return service;
  }
}
