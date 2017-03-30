/*
 * Copyright 2016-2017 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.common.utils.SslUtils;
import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.metrics.ClientMetrics;
import com.github.ibole.microservice.metrics.ClientMetrics.MetricLevel;
import com.github.ibole.microservice.rpc.core.RpcSharedThreadPools;
import com.github.ibole.microservice.rpc.core.ThreadPoolUtil;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.Recycler;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

/*********************************************************************************************
 * .
 * 
 *  
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public class GrpcClientInitializer implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(GrpcClientInitializer.class.getName());
  
  // 256 MB, server has 256 MB limit.
  private final static int MAX_MESSAGE_SIZE = 1 << 28;

  // 1 MB -- TODO: make this configurable
  private final static int FLOW_CONTROL_WINDOW = 1 << 20;

  private static final int CHANNEL_COUNT_DEFAULT = getDefaultChannelCount();

  private static SslContextBuilder sslBuilder;
  
  private static ChannelPool channelPool;
  //Corresponding to the setting of registry center server
  private static ClientOptions globalClientOptions;
  
  private static int getDefaultChannelCount() {
    // 10 Channels seemed to work well on a 4 CPU machine, and this seems to scale well for higher
    // CPU machines. Use no more than 250 Channels by default.
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    return (int) Math.min(250, Math.max(1, Math.ceil(availableProcessors * 2.5d)));
  }

  static {
    turnOffNettyRecycler();
    performWarmup();
  }

  public GrpcClientInitializer(ClientOptions pClientOptions,
      List<ClientInterceptor> clientInterceptosr, int pInitialCapacity, int pMaximumSize) {
    LOG.info("Rpc client initializer with initial capacity {} and maximum size {} for channel pool.",
        pInitialCapacity, pInitialCapacity);
    LOG.info("Global client options: \n'{}'.", pClientOptions);
    
    if (!isAlpnProviderEnabled()) {
      LOG.error(
          "Neither Jetty ALPN nor OpenSSL are available. "
          + "OpenSSL unavailability cause:\n{}",
          OpenSsl.unavailabilityCause().toString());
      throw new IllegalStateException("Neither Jetty ALPN nor OpenSSL via "
          + "netty-tcnative were properly configured.");
    }
    
    Preconditions
    .checkState(
        !AbstractNameResolverProvider.providers().isEmpty(),
        "No NameResolverProviders found via ServiceLoader, including for DNS. "
            + "This is probably due to a broken build. If using ProGuard, check your configuration");
    
    globalClientOptions = pClientOptions;
    
    channelPool = createChannelPool(globalClientOptions, clientInterceptosr, pInitialCapacity, pMaximumSize);
    
    ClientMetrics.counter(MetricLevel.Info, "Initializer.active").inc();
  }
  
  public GrpcClientInitializer(ClientOptions clientOptions, List<ClientInterceptor> clientInterceptosr) {
    this(clientOptions, clientInterceptosr, CHANNEL_COUNT_DEFAULT, CHANNEL_COUNT_DEFAULT);
  }
  
  public GrpcClientInitializer(ClientOptions clientOptions) {
    this(clientOptions, null, CHANNEL_COUNT_DEFAULT, CHANNEL_COUNT_DEFAULT);
  }
  
  public ClientOptions getGlobalClientOptions() {
    return globalClientOptions;
  }

  public ChannelPool getChannelPool() {
    return channelPool;
  }
  /**
   * The netty {@link Recycler} has caused some problems for long running operations in some
   * versions of netty. As of this comment (10/21/2016), we are using netty 4.1.3.Final. The
   * Recycler uses a system property, "io.netty.recycler.maxCapacity" which needs to be set to "0"
   * to turn off potentially problematic behavior. The string gets transformed via the shading
   * process, and ends up being similar to the Recycler's package name. This method sets the value
   * to "0" if the value is not set.
   */
  private static void turnOffNettyRecycler() {
    String packageName = Recycler.class.getName();
    String prefix = packageName.substring(0, packageName.indexOf(".util.Recycler"));
    final String key = prefix + ".recycler.maxCapacity";
    LOG.debug("Using prefix '{}' for io.netty.", prefix);
    if (System.getProperty(key) == null) {
      System.setProperty(key, "0");
    }
  }

  private synchronized static SslContext createSslContext() throws SSLException {
    if (sslBuilder == null) {
      sslBuilder = GrpcSslContexts.forClient().ciphers(null);
      // gRPC uses tcnative / OpenSsl by default, if it's available. It defaults to alpn-boot
      // if tcnative is not in the classpath.
      if (OpenSsl.isAvailable()) {
        LOG.info(
            "SslContext: gRPC is using the OpenSSL provider (tcnactive jar - Open Ssl version: {})",
            OpenSsl.versionString());
      } else {
        if (isJettyAlpnConfigured()) {
          // gRPC uses jetty ALPN as a backup to tcnative.
          LOG.info("SslContext: gRPC is using the JDK provider (alpn-boot jar)");
        } else {
          LOG.info("SslContext: gRPC cannot be configured.  Neither OpenSsl nor Alpn are available.");
        }
      }
    }
    return sslBuilder.build();
  }

  /**
   * <p>
   * isAlpnProviderEnabled.
   * </p>
   *
   * @return a boolean.
   */
  public static boolean isAlpnProviderEnabled() {
    final boolean openSslAvailable = OpenSsl.isAvailable();
    final boolean jettyAlpnConfigured = isJettyAlpnConfigured();
    LOG.debug("OpenSSL available: '{}'", openSslAvailable);
    LOG.debug("Jetty ALPN available: '{}'", jettyAlpnConfigured);
    return openSslAvailable || jettyAlpnConfigured;
  }

  /**
   * Indicates whether or not the Jetty ALPN jar is installed in the boot classloader.
   */
  private static boolean isJettyAlpnConfigured() {
    final String alpnClassName = "org.eclipse.jetty.alpn.ALPN";
    try {
      Class.forName(alpnClassName, true, null);
      return true;
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      return false;
    } catch (Exception e) {
      LOG.warn("Could not resolve alpn class: '{}'", e, alpnClassName);
      return false;
    }
  }

  private static void performWarmup() {
    // Initialize some core dependencies in parallel. This can speed up startup by 150+ ms.
    ExecutorService connectionStartupExecutor =
        Executors.newCachedThreadPool(ThreadPoolUtil
            .createThreadFactory("microservice-rpc-startup"));

    connectionStartupExecutor.execute(new Runnable() {
      @Override
      public void run() {
        // The first invocation of createSslContext() is expensive.
        // Create a throw away object in order to speed up the creation of the first
        // BigtableConnection which uses SslContexts under the covers.
        if (isAlpnProviderEnabled()) {
          try {
            // We create multiple channels via refreshing and pooling channel implementation.
            // Each one needs its own SslContext.
            createSslContext();
          } catch (SSLException e) {
            LOG.warn("Could not asynchronously create the ssl context", e);
          }
        }
      }
    });
    connectionStartupExecutor.execute(new Runnable() {
      @Override
      public void run() {
        // The first invocation of BigtableSessionSharedThreadPools.getInstance() is expensive.
        // Reference it so that it gets constructed asynchronously.
        RpcSharedThreadPools.getInstance();
      }
    });
    
    // public static final String ZK_HOST_DEFAULT = "zk1.ibole.com";
    // public static final String ZK_HOST_DEFAULT1 = "zk2.ibole.com";
    // for (final String host : Arrays.asList(ZkOptions.ZK_HOST_DEFAULT1,
    // ZkOptions.ZK_HOST_DEFAULT2)) {
    // connectionStartupExecutor.execute(new Runnable() {
    // @Override
    // public void run() {
    // // The first invocation of InetAddress retrieval is expensive.
    // // Reference it so that it gets constructed asynchronously.
    // try {
    // InetAddress.getByName(host);
    // } catch (UnknownHostException e) {
    // // ignore. This doesn't happen frequently, but even if it does, it's inconsequential.
    // }
    // }
    // });
    // }
    // connectionStartupExecutor.shutdown();
  }
  
  /**
   * Create a new {@link com.github.ibole.microservice.rpc.client.grpc.ChannelPool}.
   *
   * @param pInitialCapacity
   * @param pMaximumSize
   * @param globalClientOptions a {@link ClientOptions} object with registry center server address and other connection options.
   * @param interceptors a list of interceptor
   * @return a {@link ChannelPool} object.
   */
  private ChannelPool createChannelPool(ClientOptions globalClientOptions, List<ClientInterceptor> interceptors, int pInitialCapacity, int pMaximumSize) {
    return ChannelPool.newBuilder().withChannelFactory(new ChannelPool.ChannelFactory() {
      @Override
      public ManagedChannel create(String serviceName, String preferredZone, boolean usedTls) throws IOException {
        //build service endpoint with the default scheme and the service name provided
        String serviceEndpoint = AbstractNameResolverProvider.provider().getDefaultScheme() + "://" + serviceName;
        return createNettyChannel(globalClientOptions.withServiceEndpoint(serviceEndpoint).withZoneToPrefer(preferredZone).withUsedTls(usedTls), interceptors);
      }
    }).withInitialCapacity(pInitialCapacity).withMaximumSize(pMaximumSize).build();
  }
  
  /**
   * <p>
   * createNettyChannel.
   * </p>
   *
   * @param interceptors a {@link List} object.
   * @param globalClientOptions a {@link ClientOptions} object.
   * @return a {@link ManagedChannel} object.
   * @throws SSLException if any.
   * @throws IOException if any.
   */
  private ManagedChannel createNettyChannel(ClientOptions clientOptions, List<ClientInterceptor> interceptors) throws SSLException, IOException {
 
    NettyChannelBuilder builder = NettyChannelBuilder.forTarget(clientOptions.getServiceEndpoint());
    // 这里要注意下由于java版本的没有提供像go那样的可以指定域名
    // java版本源代码中把host传入作为证书域名
    // 域名是在证书生成的过程中自己输入的
    //String serverHostOverride = "localhost";
    if (clientOptions.getServerHostOverride() != null) {
      // Force the hostname to match the cert the server uses.
      builder.overrideAuthority(clientOptions.getServerHostOverride());
    }
    if (clientOptions.isUsedTls()) {
      builder
          .sslContext(
              GrpcSslContexts.forClient().trustManager(SslUtils.loadCert("server.pem")).build())
          .negotiationType(NegotiationType.TLS);
    }   
    builder
        .nameResolverFactory(AbstractNameResolverProvider.provider()
                .withRegistryCenterAddress(clientOptions.getRegistryCenterAddress())
                .withZoneToPrefer(clientOptions.getZoneToPrefer())
                .withServiceEndpoint(clientOptions.getServiceEndpoint())
                .withUsedTls(clientOptions.isUsedTls()))
        .idleTimeout(Long.MAX_VALUE, TimeUnit.SECONDS)
        .maxInboundMessageSize(MAX_MESSAGE_SIZE)
        //.sslContext(createSslContext())
        .eventLoopGroup(RpcSharedThreadPools.getInstance().getElg())
        .executor(RpcSharedThreadPools.getInstance().getBatchThreadPool())
        // .userAgent(VersionInfo.CORE_UESR_AGENT + "," + options.getUserAgent())
        .flowControlWindow(FLOW_CONTROL_WINDOW)
        .intercept(new HeaderClientInterceptor(),
            new StubDeadlineClientInterceptor());
    if(interceptors != null && interceptors.size() > 0){
      builder.intercept(interceptors);
    }
    return builder.build();
  }

  /* (non-Javadoc)
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    channelPool.shutdown();
    RpcSharedThreadPools.getInstance().getBatchThreadPool().shutdown();
    RpcSharedThreadPools.getInstance().getElg().shutdownGracefully();
    sslBuilder = null;
    ClientMetrics.counter(MetricLevel.Info, "Initializer.active").dec();
  }
}
