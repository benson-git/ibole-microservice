package io.ibole.microservice.rpc.server.grpc;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import org.jose4j.jwk.PublicJsonWebKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Context;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.ibole.infrastructure.cache.redis.RedisSimpleTempalte;
import io.ibole.infrastructure.common.UserPrincipalProto.AuthTokenInfo;
import io.ibole.infrastructure.common.UserPrincipalProto.UserPrincipal;
import io.ibole.infrastructure.common.exception.ErrorDetailsProto.ErrorDetails;
import io.ibole.infrastructure.common.exception.ErrorReporter;
import io.ibole.infrastructure.common.exception.TechnicalException;
import io.ibole.infrastructure.common.i18n.MessageErrorCode;
import io.ibole.infrastructure.common.properties.ConfigurationHolder;
import io.ibole.infrastructure.common.utils.Constants;
import io.ibole.infrastructure.security.jwt.JwtProvider;
import io.ibole.infrastructure.security.jwt.TokenAuthenticator;
import io.ibole.infrastructure.security.jwt.TokenParseException;
import io.ibole.infrastructure.security.jwt.TokenStatus;
import io.ibole.infrastructure.security.jwt.jose4j.JwtUtils;
import io.ibole.microservice.rpc.core.RpcContext;
import io.ibole.microservice.rpc.server.RpcServerInterceptor;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

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
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class AuthGrpcServerInterceptor implements ServerInterceptor, RpcServerInterceptor {

  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  // hold client id , login id, token information
  public static final Context.Key<UserPrincipal> USER_PRINCINPAL =
      Context.key(Constants.USER_PRINCINPAL);

  private static RedisSimpleTempalte redisTemplate;

  private static PublicJsonWebKey senderPublicJwk;

  private static PublicJsonWebKey receiverPublicJwk;

  private static Metadata.Key<UserPrincipal> userPrincipalKey =
      ProtoUtils.keyForProto(UserPrincipal.getDefaultInstance());

  private static Metadata.Key<ErrorDetails> errorDetailsKey =
      ProtoUtils.keyForProto(ErrorDetails.getDefaultInstance());
  
  @SuppressWarnings("rawtypes")
  private static TokenAuthenticator tokenAuthenticator;

  /**
   * Default constructor. 
   * <p>1. connect to redis server 
   * <p>2. init sender/receiver public json web key
   */
  public AuthGrpcServerInterceptor() {
    // 连接redis服务
    String redisHost = ConfigurationHolder.get().get(Constants.CACHE_REDIS_SERVER);
    int redisPort = Integer.parseInt(ConfigurationHolder.get().get(Constants.CACHE_REDIS_PORT));
    String password = ConfigurationHolder.get().get(Constants.CACHE_REDIS_PASSWORD);
    redisTemplate = new RedisSimpleTempalte(redisHost, redisPort, password);
    // cache sender/receiver jwk
    try {
      senderPublicJwk = JwtUtils
          .toJsonWebKey(getClass().getResource(Constants.SENDER_JWK_PATH).toURI().getPath());
      receiverPublicJwk = JwtUtils
          .toJsonWebKey(getClass().getResource(Constants.RECEIVER_JWK_PATH).toURI().getPath());
      
      tokenAuthenticator =
          JwtProvider.provider().createTokenGenerator(redisTemplate);

    } catch (URISyntaxException ex) {
      throw new TechnicalException(ErrorReporter.INTERNAL.withCause(ex));
    }

  }

  @SuppressWarnings({"unchecked"})
  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
      Metadata requestHeaders, ServerCallHandler<ReqT, RespT> next) {
    String serviceRpcName = call.getMethodDescriptor().getFullMethodName();
    UserPrincipal userPrincipal = requestHeaders.get(userPrincipalKey);
    if (userPrincipal == null || Strings.isNullOrEmpty(userPrincipal.getClientId())) {
      Metadata trailers = new Metadata();
      trailers.put(errorDetailsKey, ErrorReporter.UNAUTHENTICATED
          .withSpecificErrorMsg(MessageErrorCode.CLIENT_ID_REQUIRED_KEY, true).toErrorDetails());
      call.close(Status.UNAUTHENTICATED, trailers);
      return new ServerCall.Listener<ReqT>() {};
    }
    // skip the token validation if it is a login rpc service or it is anonymous access
    if (!serviceRpcName.toLowerCase().contains("login")
        && !Constants.ANONYMOUS_ID.equalsIgnoreCase(userPrincipal.getLoginId())) {
      final Stopwatch stopwatch = Stopwatch.createStarted();
      TokenStatus tokenStatus = tokenAuthenticator.validAccessToken(
          userPrincipal.getAuthToken().getAccessToken(), userPrincipal.getClientId(),
          userPrincipal.getLoginId(), senderPublicJwk, receiverPublicJwk);
      String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
      logger.info("AuthGrpcServerInterceptor elapsed time: {} ms", elapsedString);
      if (!TokenStatus.VALIDATED.getCode().equals(tokenStatus.getCode())) {
        // handle expired access token
        if (TokenStatus.ACCESS_TOKEN_EXPIRED.getCode().equals(tokenStatus.getCode())) {
          Metadata trailers = new Metadata();
          try {
            String accessToken =
                tokenAuthenticator.renewToken(userPrincipal.getAuthToken().getAccessToken(),
                    Integer.parseInt(ConfigurationHolder.get().get(Constants.ACCESS_TOKEN_TTL)),
                    false, senderPublicJwk, receiverPublicJwk);
            userPrincipal = userPrincipal.toBuilder().setAuthToken(
                AuthTokenInfo.newBuilder().setAccessToken(accessToken).setRenewAccessToken(true))
                .build();
          } catch (NumberFormatException | TokenParseException ex) {
            logger.error("Failed to renew access token", ex);
            trailers.put(errorDetailsKey,
                ErrorReporter.UNAUTHENTICATED
                    .withSpecificErrorMsg(MessageErrorCode.ACCESS_TOKEN_RENEW_FAILED_KEY, true)
                    .toErrorDetails());
            userPrincipal = userPrincipal.toBuilder()
                .setAuthToken(AuthTokenInfo.newBuilder().setLoginRequired(true)).build();
            call.close(Status.UNAUTHENTICATED, trailers);
            return new ServerCall.Listener<ReqT>() {};
          }
        } else {
          // client side need to do the login with their credential(loginId,password)
          userPrincipal = userPrincipal.toBuilder()
              .setAuthToken(AuthTokenInfo.newBuilder().setLoginRequired(true)).build();
          return handleAuthTokenException(call, tokenStatus);
        }
      }
    }
    final UserPrincipal updatedUserPrincipal = userPrincipal;
    Context context = Context.current().withValue(USER_PRINCINPAL, userPrincipal);
    Context previous = context.attach();

    final ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
          @Override
          public void sendHeaders(Metadata responseHeaders) {
            UserPrincipal loginUserPrincipal = (UserPrincipal) RpcContext.getData().get(Constants.USER_PRINCINPAL);
            responseHeaders.put(userPrincipalKey, loginUserPrincipal == null? updatedUserPrincipal : loginUserPrincipal);
            super.sendHeaders(responseHeaders);
          }
          @Override
          public void close(Status status, Metadata trailers) {
            try {
              delegate().close(status, trailers);
            } finally {
              RpcContext.getData().clear();
            }
          }
        };

    try {
      return new ContextualizedServerCallListener<ReqT>(next.startCall(wrappedCall, requestHeaders),
          context);
    } finally {
      context.detach(previous);
    }
  }

  /**
   * Handle AuthToken Exception.
   * <p>
   * 1. illegal token, need to ask client side to relogin
   * </p>
   * <p>
   * 2. refresh token is expired, need to ask client side to relogin
   * </p>
   * 
   * @param call
   * @param userPrincipal
   * @param tokenAuthenticator
   * @param tokenStatus
   * @return Listener<ReqT>
   */
  private <ReqT, RespT> Listener<ReqT> handleAuthTokenException(ServerCall<ReqT, RespT> call, TokenStatus tokenStatus) {
    Metadata trailers = new Metadata();
    // illegal rpc access or the refresh token is expired
    if (TokenStatus.ILLEGAL.getCode().equals(tokenStatus.getCode())
        || TokenStatus.REFRESH_TOKEN_EXPIRED.getCode().equals(tokenStatus.getCode())) {
      trailers.put(errorDetailsKey, ErrorReporter.UNAUTHENTICATED
          .withSpecificErrorMsg(MessageErrorCode.ERROR_UNAUTHENTICATED_KEY, true).toErrorDetails());
      call.close(Status.UNAUTHENTICATED, trailers);
    }
    return new ServerCall.Listener<ReqT>() {};
  }

  /**
   * Implementation of {@link io.grpc.ForwardingServerCallListener} that attaches a context before
   * dispatching calls to the delegate and detaches them after the call completes.
   */
  private static class ContextualizedServerCallListener<ReqT>
      extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
    private final Context context;

    public ContextualizedServerCallListener(ServerCall.Listener<ReqT> delegate, Context context) {
      super(delegate);
      this.context = context;
    }

    @Override
    public void onMessage(ReqT message) {
      Context previous = context.attach();
      try {
        super.onMessage(message);
      } finally {
        context.detach(previous);
      }
    }

    @Override
    public void onHalfClose() {
      Context previous = context.attach();
      try {
        super.onHalfClose();
      } finally {
        context.detach(previous);
      }
    }

    @Override
    public void onCancel() {
      Context previous = context.attach();
      try {
        super.onCancel();
      } finally {
        context.detach(previous);
      }
    }

    @Override
    public void onComplete() {
      Context previous = context.attach();
      try {
        super.onComplete();
      } finally {
        context.detach(previous);
      }
    }

    @Override
    public void onReady() {
      Context previous = context.attach();
      try {
        super.onReady();
      } finally {
        context.detach(previous);
      }
    }
  }

}
