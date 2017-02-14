//package io.ibole.microservice.rpc.client.grpc;
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
// * 版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
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
