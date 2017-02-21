package io.ibole.microservice.rpc.server.grpc;
import com.google.protobuf.InvalidProtocolBufferException;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.ibole.infrastructure.common.MiniDeviceInfoProto.MiniDeviceInfo;
import io.ibole.infrastructure.common.exception.ErrorReporter;
import io.ibole.infrastructure.common.exception.TechnicalException;
import io.ibole.infrastructure.common.utils.Constants;
import io.ibole.microservice.rpc.server.RpcServerInterceptor;


/**
 * A interceptor to handle server header.
 * 
 * Intercept the device information of the caller.
 * 
 */
public class DeviceInfoGrpcServerInterceptor implements ServerInterceptor, RpcServerInterceptor {
  
  public static final Context.Key<MiniDeviceInfo> MINI_DEVICE_INFO = Context.key(Constants.MINI_DEVICE_INFO); 
 
  private static final Metadata.BinaryMarshaller<MiniDeviceInfo> DEVICE_MARSHALLER =
      new Metadata.BinaryMarshaller<MiniDeviceInfo>() {
        @Override
        public byte[] toBytes(MiniDeviceInfo info) {
          return info.toByteArray();
        }

        @Override
        public MiniDeviceInfo parseBytes(byte[] serialized) {
          try {
            return MiniDeviceInfo.newBuilder().mergeFrom(serialized).build();
          } catch (InvalidProtocolBufferException ex) {
            throw new TechnicalException(ErrorReporter.INTERNAL.withCause(ex));
          }
        }
      };

  private static Metadata.Key<MiniDeviceInfo> miniDeviceInfoKey = Metadata.Key.of(
      "miniDeviceInfo_key-bin", DEVICE_MARSHALLER);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
      final Metadata requestHeaders, ServerCallHandler<ReqT, RespT> next) {
    
    Context context = Context.current().withValue(MINI_DEVICE_INFO, requestHeaders.get(miniDeviceInfoKey));
  
    return Contexts.interceptCall(context, call, requestHeaders, next);
  }
}
