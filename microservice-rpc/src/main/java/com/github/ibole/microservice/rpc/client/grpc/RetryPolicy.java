//package com.github.ibole.microservice.rpc.client.grpc;
//
//import io.grpc.CallOptions;
//import io.grpc.MethodDescriptor;
//import io.grpc.Status;
//import  io.grpc.internal.BackoffPolicy;
//
///*********************************************************************************************
// * .
// * 
// * 
// * <p>
// * Copyright 2016, iBole Inc. All rights reserved.
// * 
// * <p>
// * </p>
// *********************************************************************************************/
//
//
//public abstract class RetryPolicy {
//  public interface Provider {
//    RetryPolicy get();
//  }
//
//  private final BackoffPolicy backoffPolicy;
//
//  RetryPolicy(BackoffPolicy.Provider backoffPolicy) {
//    this.backoffPolicy = backoffPolicy.get();
//  }
//
//  RetryPolicy() {
//    this(new ExponentialBackoffPolicy.CallRetryProvider());
//  }
//
//  public long getNextBackoffMillis() {
//    return backoffPolicy.nextBackoffMillis();
//  }
//
//  public abstract boolean isRetryable(Status status, MethodDescriptor method,
//      CallOptions callOptions);
//}
