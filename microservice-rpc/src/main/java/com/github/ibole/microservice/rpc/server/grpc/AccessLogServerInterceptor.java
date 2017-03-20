package com.github.ibole.microservice.rpc.server.grpc;

import com.github.ibole.microservice.rpc.server.RpcServerInterceptor;

import com.google.common.base.Stopwatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/

/**
 * Interceptor for logging all clients' accesses similar to Apache access.log. Logging format is as
 * follows,
 * 
 * <pre>
 * {@code
 * [service/method] [client ip] [user agent] [status] [elapsed time] [headers if trace logging is enabled]
 * }
 * </pre>
 *
 * @see ServerInterceptors for instructions on how to add interceptors to a server
 */

public final class AccessLogServerInterceptor  implements ServerInterceptor, RpcServerInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger("AccessLog");

  private static final String UNKNOWN_IP = "unknown-ip";
  private static final String UNKNOWN_USER_AGENT = "unknown-user-agent";

  public static final String GRPC_RPC_NAME_KEY = "grpcRpcName";
  public static final String GRPC_CLIENT_IP_KEY = "grpcClientIp";
  public static final String GRPC_USER_AGENT_KEY = "grpcUserAgent";
  public static final String GRPC_STATUS_KEY = "grpcStatus";
  public static final String GRPC_ELAPSED_MS_KEY = "grpcElapsedMs";
  public static final String GRPC_HEADERS_KEY = "grpcHeaders";
  
  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call,
      final Metadata headers, final ServerCallHandler<ReqT, RespT> next) {
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final String clientIp = clientIp(call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));
    final String userAgent =
        Optional.ofNullable(headers.get(GrpcUtil.USER_AGENT_KEY)).orElse(UNKNOWN_USER_AGENT);
    final String serviceRpcName = call.getMethodDescriptor().getFullMethodName();

    final ServerCall<ReqT, RespT> wrappedCall =
        new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
          @Override
          public void close(Status status, Metadata trailers) {
            logCallEnded(serviceRpcName, clientIp, userAgent, status, stopwatch, headers);
            super.close(status, trailers);
          }
        };
    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
        next.startCall(wrappedCall, headers)) {
      @Override
      public void onCancel() {
        logCallEnded(serviceRpcName, clientIp, userAgent, Status.CANCELLED, stopwatch, headers);
        super.onCancel();
      }

      @Override
      public void onHalfClose() {
        try {
          super.onHalfClose();
        } catch (Exception e) {
          logCallEnded(serviceRpcName, clientIp, userAgent, Status.UNKNOWN, stopwatch, headers);
          throw e;
        }
      }

      @Override
      public void onMessage(ReqT message) {
        try {
          super.onMessage(message);
        } catch (Exception e) {
          logCallEnded(serviceRpcName, clientIp, userAgent, Status.UNKNOWN, stopwatch, headers);
          throw e;
        }
      }
    };
  }

  private String clientIp(@Nullable final SocketAddress socketAddress) {
    if (socketAddress == null) {
      return UNKNOWN_IP;
    }

    if (!(socketAddress instanceof InetSocketAddress)) {
      return socketAddress.toString();
    }

    final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
    final String hostString = inetSocketAddress.getHostString();
    return hostString == null ? UNKNOWN_IP : hostString;
  }

  private void logCallEnded(String serviceRpcName, String clientIp, String userAgent, Status status,
      Stopwatch stopwatch, Metadata headers) {
    MDC.put(GRPC_RPC_NAME_KEY, serviceRpcName);
    MDC.put(GRPC_CLIENT_IP_KEY, clientIp);
    MDC.put(GRPC_USER_AGENT_KEY, userAgent);
    String statusString = status.getCode().name();
    MDC.put(GRPC_STATUS_KEY, statusString);
    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    MDC.put(GRPC_ELAPSED_MS_KEY, elapsedString);
    if (LOG.isTraceEnabled()) {
      String headerString = headers.toString();
      MDC.put(GRPC_HEADERS_KEY, headerString);
      LOG.trace("[{}] [{}] [{}] [{}] [{} ms] [{}]", serviceRpcName, clientIp, userAgent,
          statusString, elapsedString, headerString);
    } else {
      LOG.info("[{}] [{}] [{}] [{}] [{} ms]", serviceRpcName, clientIp, userAgent, statusString,
          elapsedString);
    }
  }
}
