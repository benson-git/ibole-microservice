package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.config.rpc.client.RpcClient;
import com.github.ibole.microservice.config.rpc.client.RpcClientProvider;

import io.grpc.stub.AbstractStub;

public class GrpcClientProvider extends RpcClientProvider {

  @Override
  protected boolean isAvailable() {

    return true;
  }
  
  @Override
  protected int priority() {

    return 5;
  }

  @Override
  public RpcClient<? extends AbstractStub<?>> getRpcClient() {

    return GrpcClient.getInstance();
  }

}  
