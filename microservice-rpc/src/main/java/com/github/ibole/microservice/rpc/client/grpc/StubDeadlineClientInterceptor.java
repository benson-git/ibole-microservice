package com.github.ibole.microservice.rpc.client.grpc;



import io.grpc.CallOptions;
import io.grpc.CallOptions.Key;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;

import java.util.concurrent.TimeUnit;

/*********************************************************************************************
 * .
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * Implement an interceptor that customizes the CallOptions passed to the underlying channel to
 * enforce the desired deadline for each RPC call.
 * 
 * @author bwang
 *
 */
public class StubDeadlineClientInterceptor extends AbstractGrpcClientInterceptor {

  /**
   * Key for deadline duration, unit： MILLISECONDS
   */
  public static final Key<Integer> DEADLINE_KEY = Key.of("deadlineKey", 3000);

  /*
   * (non-Javadoc)
   * 
   * @see io.grpc.ClientInterceptor#interceptCall(io.grpc.MethodDescriptor, io.grpc.CallOptions,
   * io.grpc.Channel)
   */
  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions, Channel next) {
    Integer timeout = callOptions.getOption(DEADLINE_KEY);
    CallOptions callOptionsWithTimeout = null != timeout && timeout > 0 ? 
        callOptions.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS) : callOptions;
    return next.newCall(method, callOptionsWithTimeout);
  }

}
