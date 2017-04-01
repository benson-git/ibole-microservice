package com.github.ibole.microservice.registry.service.grpc;

/**
 * Some naming Conventions for the generated Grpc class.
 * 
 * @author bwang
 *
 */
public final class GrpcConstants {
  //Server side constants
  public static final String PROTO_DESC_FILENAME = "protos.desc";
  public static final String SERVICE_CLAZZ_SUFFIX = "Grpc";
  public static final String SERVICE_IMPL_CLAZZ_SUFFIX = "ImplBase";
  public static final String SERVICE_BIND_METHOD = "bindService";
  //Client side constants
  public static final String SERVICE_NAME = "SERVICE_NAME";
  public static final String CLIENT_STUB_SUFFIX_BLOCKING = "BlockingStub";
  public static final String CLIENT_STUB_SUFFIX_FUTURE = "FutureStub";
  public static final String CLIENT_STUB_SUFFIX_ASYN = "Stub";
  //blocking-style stub
  public static final String NEW_CLIENT_BLOCKING_STUB = "newBlockingStub";
  //async-style stub
  public static final String NEW_CLIENT_ASYN_STUB = "newStub";
  //Google ListenableFuture-style stub
  public static final String NEW_CLIENT_FUTURE_STUB = "newFutureStub";

}
