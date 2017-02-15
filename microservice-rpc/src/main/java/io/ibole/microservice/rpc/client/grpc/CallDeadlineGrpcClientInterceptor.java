package io.ibole.microservice.rpc.client.grpc;



import io.ibole.infrastructure.common.MiniDeviceInfoProto.MiniDeviceInfo;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Metadata.AsciiMarshaller;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 *  Implement an interceptor that customizes the CallOptions passed to
 *  the underlying channel to enforce the desired deadline for each RPC call.
 *   
 * @author bwang
 *
 */
public class CallDeadlineGrpcClientInterceptor extends AbstractGrpcClientInterceptor {

  /**
   * Deadline duration, unit： MILLISECONDS
   */
  public static final String DEADLINE_KEY = "deadlineKey";
  
  /* (non-Javadoc)
   * @see io.grpc.ClientInterceptor#interceptCall(io.grpc.MethodDescriptor, io.grpc.CallOptions, io.grpc.Channel)
   */
  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions, Channel next) {
    // TODO Auto-generated method stub
    return null;
  }

}
