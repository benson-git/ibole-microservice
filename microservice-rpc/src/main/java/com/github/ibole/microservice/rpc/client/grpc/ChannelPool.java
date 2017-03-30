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

package com.github.ibole.microservice.rpc.client.grpc;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.ibole.microservice.metrics.ClientMetrics;
import com.github.ibole.microservice.metrics.ClientMetrics.MetricLevel;
import com.github.ibole.microservice.metrics.Counter;
import com.github.ibole.microservice.metrics.Meter;
import com.github.ibole.microservice.metrics.Timer;
import com.github.ibole.microservice.metrics.Timer.Context;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptors.CheckedForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public class ChannelPool {
  
  /** Constant <code>CHANNEL_ID_KEY</code> */
  private static final Key<String> CHANNEL_ID_KEY = Key.of("grpc-channel-id", Metadata.ASCII_STRING_MARSHALLER);
  
  private static final AtomicInteger ChannelIdGenerator = new AtomicInteger();

  protected static Stats STATS;

  private static class Stats {
    /**
     * Best effort counter of active channels. There may be some cases where channel termination
     * counting may not accurately be decremented.
     */
    Counter ACTIVE_CHANNEL_COUNTER = ClientMetrics.counter(MetricLevel.Info, "grpc.channel.active");

    /**
     * Best effort counter of active RPCs.
     */
    Counter ACTIVE_RPC_COUNTER = ClientMetrics.counter(MetricLevel.Info, "grpc.rpc.active");

    /**
     * Best effort counter of RPCs.
     */
    Meter RPC_METER = ClientMetrics.meter(MetricLevel.Info, "grpc.rpc.performed");
  }

  protected static synchronized Stats getStats() {
    if (STATS == null) {
      STATS = new Stats();
    }
    return STATS;
  }
  
  public static final String extractIdentifier(Metadata trailers) {
    return trailers != null ? trailers.get(ChannelPool.CHANNEL_ID_KEY) : "";
  }
  
  private final Cache<String, InstrumentedChannel> channelPool;
  
  private final ChannelFactory factory;
  
  public static Builder newBuilder(){
    return new Builder();
  }
  
  /**
   * <p>Constructor for ChannelPool.</p>
   *
   * @param factory a {@link com.github.ibole.microservice.rpc.client.grpc.ChannelPool.ChannelFactory} object.
   * @param initialCapacity the initial capacity of the channel pool
   * @param maximumSize the maximum size of the channel pool
   * @return the instance of ChannelPool
   * @throws java.io.IOException if any.
   */
  private ChannelPool(ChannelFactory factory, int initialCapacity, int maximumSize) {
    Preconditions.checkArgument(factory != null,
        "ChannelFactory cannot be null.");
    Preconditions.checkArgument(initialCapacity > 0,
        "Channel initial capacity has to be a positive number.");
    Preconditions.checkArgument(maximumSize > 0,
        "Channel maximum size has to be a positive number.");
    Preconditions
        .checkArgument(maximumSize >= initialCapacity,
            "The maximum size of channel pool has to be greater than or equal to the initial capacity.");
    
    Caffeine<String, InstrumentedChannel> caffeine = Caffeine.newBuilder().initialCapacity(initialCapacity).maximumSize(maximumSize).weakKeys()
        .softValues().removalListener(new ChannelRemovalListener());
    this.factory = factory;
    channelPool = caffeine.build();
  }
  
  /**
   * Get existing channel from the pool if expected channel is found in the pool, otherwise create a
   * new channel with the specified service name and return it to caller.
   * 
   * @param serviceName the service name
   * @param preferredZone the preferred zone, if provided, it will override the one in global client options
   * @param usedTls if need to use TLS, if provided, it will override the one in global client options
   * @return the instance of ManagedChannel mapping with the service name, or create a new channel
   *         with the specified service name if no mapping for the key
   * @throws IOException if I/O exception happen
   */
  public ManagedChannel getChannel(String serviceName, String preferredZone, boolean usedTls) throws IOException {
    InstrumentedChannel channel = channelPool.getIfPresent(serviceName);
    if(channel != null){
      return channel;
    } else {
      channel = new InstrumentedChannel(factory.create(serviceName, preferredZone, usedTls));
      channelPool.put(serviceName, channel);
      return channel;
    }
  }
  
  public long size() {
    return channelPool.estimatedSize();
  }
  
  public void shutdownNow() {
    channelPool.asMap().forEach((key, value) -> {
      if (!value.isTerminated()) {
        value.shutdownNow();
      }
    });
  }

  // graceful shutdown
  public void shutdown() {
    channelPool.asMap().forEach((key, value) -> {
      if (!value.isTerminated()) {
        try {
          value.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
    });
    channelPool.cleanUp();
  }
  
  class ChannelRemovalListener implements RemovalListener<String, InstrumentedChannel>{
    @Override
    public void onRemoval(String key, InstrumentedChannel value, RemovalCause cause) {
      value.shutdownNow(); 
    }
    
  }
  
  public static class Builder {
    
    private ChannelFactory factory;
    private int initialCapacity;
    private int maximumSize;
    
    public ChannelPool build() {
      return new ChannelPool(factory, initialCapacity, maximumSize);
    }
    
    public Builder withInitialCapacity(int pInitialCapacity){
      initialCapacity = pInitialCapacity;
      return this;
    }
    
    public Builder withMaximumSize(int pMaximumSize){
      maximumSize = pMaximumSize;
      return this;
    }
    
    public Builder withChannelFactory(ChannelFactory pFactory){
      factory = pFactory;
      return this;
    }
  }

  /**
   * Contains a {@link ManagedChannel} and metrics for the channel
   *
   */
  @VisibleForTesting
  protected class InstrumentedChannel extends ManagedChannel {
    private final ManagedChannel delegate;
    // a uniquely named timer for this channel's latency
    private final Timer timer;

    private final AtomicBoolean active = new AtomicBoolean(true);
    private final int channelId;

    public InstrumentedChannel(ManagedChannel channel) {
      this.delegate = channel;
      this.channelId = ChannelIdGenerator.incrementAndGet();
      this.timer = ClientMetrics.timer(MetricLevel.Trace, "channels.channel" + channelId + ".rpc.latency");
      getStats().ACTIVE_CHANNEL_COUNTER.inc();
    }

    private synchronized void markInactive(){
      boolean previouslyActive = active.getAndSet(false);
      if (previouslyActive) {
        getStats().ACTIVE_CHANNEL_COUNTER.dec();
      }
    }

    @Override
    public ManagedChannel shutdown() {
      markInactive();
      return delegate.shutdown();
    }

    @Override
    public boolean isShutdown() {
      return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
      return delegate.isTerminated();
    }

    @Override
    public ManagedChannel shutdownNow() {
      markInactive();
      return delegate.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException  {
      markInactive();
      return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT>
        newCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions) {
      final Context timerContext = timer.time();
      final AtomicBoolean decremented = new AtomicBoolean(false);
      return new CheckedForwardingClientCall<ReqT, RespT>(delegate.newCall(methodDescriptor, callOptions)) {
        @Override
        protected void checkedStart(ClientCall.Listener<RespT> responseListener, Metadata headers)
            throws Exception {
          ClientCall.Listener<RespT> timingListener = wrap(responseListener, timerContext, decremented);
          getStats().ACTIVE_RPC_COUNTER.inc();
          getStats().RPC_METER.mark();
          delegate().start(timingListener, headers);
        }

        @Override
        public void cancel(String message, Throwable cause) {
          if (!decremented.getAndSet(true)) {
            getStats().ACTIVE_RPC_COUNTER.dec();
          }
          super.cancel(message, cause);
        }
      };
    }

    protected <RespT> ClientCall.Listener<RespT> wrap(final ClientCall.Listener<RespT> delegate,
        final Context timeContext, final AtomicBoolean decremented) {
      return new ClientCall.Listener<RespT>() {

        @Override
        public void onHeaders(Metadata headers) {
          delegate.onHeaders(headers);
        }

        @Override
        public void onMessage(RespT message) {
          delegate.onMessage(message);
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
          try {
            if (trailers != null) {
              // Be extra defensive since this is only used for logging
              trailers.put(CHANNEL_ID_KEY, Integer.toString(channelId));
            }
            if (!decremented.getAndSet(true)) {
              getStats().ACTIVE_RPC_COUNTER.dec();
            }
            if (!status.isOk()) {
              ClientMetrics.meter(MetricLevel.Info, "grpc.errors." + status.getCode().name())
                  .mark();
            }
            delegate.onClose(status, trailers);
          } finally {
            timeContext.close();
          }
        }

        @Override
        public void onReady() {
          delegate.onReady();
        }
      };
    }

    @Override
    public String authority() {
      return delegate.authority();
    }
  }
  
  /**
   * A factory for creating ManagedChannels to be used in a {@link ChannelPool}.
   *
   *
   */
  public interface ChannelFactory {
    
    ManagedChannel create(String serviceName, String preferredZone, boolean usedTls) throws IOException;
  }
}
