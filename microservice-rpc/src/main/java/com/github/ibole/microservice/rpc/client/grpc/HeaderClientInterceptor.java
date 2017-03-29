package com.github.ibole.microservice.rpc.client.grpc;
import com.google.protobuf.InvalidProtocolBufferException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import com.github.ibole.infrastructure.common.MiniDeviceInfoProto.MiniDeviceInfo;
import com.github.ibole.infrastructure.common.UserPrincipalProto.UserPrincipal;
import com.github.ibole.infrastructure.common.dto.MiniDeviceInfoTo;
import com.github.ibole.infrastructure.common.exception.ErrorDetailsProto.ErrorDetails;
import com.github.ibole.infrastructure.common.utils.Constants;
import com.github.ibole.microservice.rpc.core.RpcContext;


/**
 * 
 * A interceptor to handle client header.
 * 
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class HeaderClientInterceptor extends AbstractGrpcClientInterceptor {

  private static final Logger LOG =
      LoggerFactory.getLogger(HeaderClientInterceptor.class.getName());

  private static Metadata.Key<UserPrincipal> userPrincipalKey =
      ProtoUtils.keyForProto(UserPrincipal.getDefaultInstance());

  private static Metadata.Key<ErrorDetails> errorDetailsKey =
      ProtoUtils.keyForProto(ErrorDetails.getDefaultInstance());

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
            throw new RuntimeException(ex);
          }
        }
      };
  private static Metadata.Key<MiniDeviceInfo> miniDeviceInfoKey =
      Metadata.Key.of("miniDeviceInfo_key-bin", DEVICE_MARSHALLER);

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions, Channel next) {
    return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        /* put client id and login id to header */
        UserPrincipal userPrincipal = (UserPrincipal) RpcContext.getData().get(Constants.USER_PRINCINPAL);
        if (userPrincipal != null) {
          headers.put(userPrincipalKey, userPrincipal);
        }
        /* add device information to header */
        MiniDeviceInfoTo deviceInfo = (MiniDeviceInfoTo) RpcContext.getData().get(Constants.MINI_DEVICE_INFO);
        if (deviceInfo != null) {
          MiniDeviceInfo miniDeviceInfo = MiniDeviceInfo.newBuilder()
              .setModel(deviceInfo.getModel() == null ? "" : deviceInfo.getModel())
              .setOsVersion(deviceInfo.getOsVersion()).setVersionCode(deviceInfo.getVersionCode())
              .setImei(deviceInfo.getImei() == null ? "" : deviceInfo.getImei())
              .setIpAddress(deviceInfo.getIpAddress() == null ? "" : deviceInfo.getIpAddress())
              .build();
          headers.put(miniDeviceInfoKey, miniDeviceInfo);
        }
        RpcContext.getData().clear();
        super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
          @Override
          public void onHeaders(Metadata headers) {
            /**
             * if you don't need receive header from server, you can use
             * {@link io.grpc.stub.MetadataUtils attachHeaders} directly to send header
             */
            UserPrincipal userPrincipal = headers.get(userPrincipalKey);
            if (userPrincipal != null) {
              LOG.info("Token renew?: " + userPrincipal.getAuthToken().getRenewAccessToken());
            }
            super.onHeaders(headers);
          }

          @Override
          public void onClose(Status status, Metadata trailers) {
            if (!status.isOk()) {
              ErrorDetails errorDetails = trailers.get(errorDetailsKey);
              if (errorDetails != null) {
                String errors = errorDetails.getSpecificMessage();
                if (errorDetails.getDetailedMessageList().size() > 0) {
                  LOG.error("Error happened from service server: " + errors + "; Root cause: "
                      + errorDetails.getDetailedMessage(0));
                }
              }
            }
            super.onClose(status, trailers);
          }
        }, headers);
      }
    };
  }


}
