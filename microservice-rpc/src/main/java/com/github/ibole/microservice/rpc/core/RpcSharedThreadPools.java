/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ibole.microservice.rpc.core;

import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * This class contains executors and other thread pool related resources that can be reused across a
 * few {@link com.google.cloud.bigtable.grpc.BigtableSession}s.  
 * All executors are automatically expand if there is higher use.
 * 
 *
 * @author bwang
 *
 */
public class RpcSharedThreadPools {
 
  public static final String BATCH_POOL_THREAD_NAME = "microservices-batch-pool";

  public static final String RPC_EVENTLOOP_GROUP_NAME = "microservices-rpc-elg";

  private static RpcSharedThreadPools INSTANCE = new RpcSharedThreadPools();

  /**
   * This is used to do pre and post RPC work, and not the i/o itself.
   */
  protected ExecutorService batchThreadPool;

  /**
   * This is needed by nio. We create daemon threads rather than default threads so that if a user
   * shuts down a JVM, the microservices connection doesn't block the shutdown. By default, the ELG is
   * not a daemon thread pool.
   */
  protected NioEventLoopGroup elg;
  
  /**
   * <p>Constructor for BigtableSessionSharedThreadPools.</p>
   */
  protected RpcSharedThreadPools() {
    init();
  }

  /**
   * <p>init.</p>
   */
  protected void init() {
    batchThreadPool = Executors.newCachedThreadPool(createThreadFactory(BATCH_POOL_THREAD_NAME));
    elg = new NioEventLoopGroup(0, createThreadFactory(RPC_EVENTLOOP_GROUP_NAME));
  }

  /**
   * <p>createThreadFactory.</p>
   *
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.util.concurrent.ThreadFactory} object.
   */
  protected ThreadFactory createThreadFactory(String name) {
    return ThreadPoolUtil.createThreadFactory(name);
  }
  
  /**
   * Get the shared instance of ThreadPools.
   *
   * @return a {@link com.github.ibole.microservice.rpc.core.RpcSharedThreadPools} object.
   */
  public static RpcSharedThreadPools getInstance() {
    return INSTANCE;
  }
  
  /**
   * <p>Getter for the field <code>batchThreadPool</code>.</p>
   *
   * @return a {@link java.util.concurrent.ExecutorService} object.
   */
  public ExecutorService getBatchThreadPool() {
    return batchThreadPool;
  }

  /**
   * <p>Getter for the field <code>elg</code>.</p>
   *
   * @return a {@link io.netty.channel.nio.NioEventLoopGroup} object.
   */
  public NioEventLoopGroup getElg() {
    return elg;
  }
}
