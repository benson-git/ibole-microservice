package io.ibole.microservice.rpc.client.grpc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.NameResolverProvider;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.AbstractStub;
import io.ibole.infrastructure.common.utils.Tuple;
import io.ibole.microservice.common.ServerIdentifier;
import io.ibole.microservice.common.utils.ClassHelper;
import io.ibole.microservice.common.utils.SslUtils;
import io.ibole.microservice.config.rpc.client.RpcClient;
import io.ibole.microservice.discovery.DiscoveryFactory;
import io.ibole.microservice.discovery.InstanceMetadata;
import io.ibole.microservice.discovery.ServiceDiscovery;
import io.ibole.microservice.discovery.ServiceDiscoveryProvider;
import io.ibole.microservice.registry.instance.grpc.GrpcConstants;
import io.ibole.microservice.rpc.client.exception.RpcClientException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLException;

/**
 * RPC client helper.
 * 
 * @author bwang
 *
 */
public final class GrpcClient implements RpcClient<AbstractStub<?>> {

  private static Logger log = LoggerFactory.getLogger(GrpcClient.class.getName());
  // Cache the mapping between service type and the tuple structure:
  // {X: rpc servers connection key Y: the method for initializing client stub}
  private static final Map<Class<? extends AbstractStub<?>>, Tuple<String, Method>> STUBS = Maps.newConcurrentMap();
  // Cache the mapping between rpc servers connection key and channel instance
  private static final Map<String, ManagedChannel> CHANNELS = Maps.newConcurrentMap();
 
  private final AtomicReference<State> state = new AtomicReference<State>(State.LATENT);
  
  private static ServiceDiscovery<InstanceMetadata> discovery = null;
  
  private GrpcClient() {
    // do nothing.
  }

  enum State {

    LATENT, INITIALIZED, STARTED, STOPPED;

  }

  /**
   * The server identifier is used to connect to registry center.
   */
  @Override
  public void initialize(ServerIdentifier identifier) {
    if (state.compareAndSet(State.LATENT, State.INITIALIZED)
        || state.compareAndSet(State.STOPPED, State.INITIALIZED)) {
      DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> factory =
          ServiceDiscoveryProvider.provider().getDiscoveryFactory();
      discovery = factory.getServiceDiscovery(identifier);
    }
  }

  /**
   * Start RPC client.
   */
  public void start() {
    if (!state.compareAndSet(State.INITIALIZED, State.STARTED)) {
      return;
    }
    discovery.start();
    if (log.isInfoEnabled()) {
      log.info("Rpc client is started.");
    }
  }

  /**
   * Stop RPC client.
   */
  public void stop() {
    
    if (!state.compareAndSet(State.STARTED, State.STOPPED)) {
      return;
    }
    
    try {
      discovery.destroy();
      for (String key : CHANNELS.keySet()) {
        CHANNELS.get(key).shutdown().awaitTermination(5, TimeUnit.SECONDS);
      }
      CHANNELS.clear();
      STUBS.clear();
    } catch (Exception ex) {
      log.error("Rpc client stop error happened", ex);
      throw new RpcClientException(ex);
    }
  
    if (log.isInfoEnabled()) {
      log.info("Rpc client is stopped.");
    }
  }

  /**
   * Get remoting service instance for client invocation.
   * 
   * @param type the type of expected service instance
   * @param timeout (millisecond) specify the remoting call will be expired at the specified offset from now
   * @return T the instance of T.
   */
  @Override
  public AbstractStub<?> getRemotingService(Class<? extends AbstractStub<?>> type, int timeout) {
    checkArgument(type != null, "The type of service interface cannot be null!");
    checkState(state.get() == State.STARTED, "Grpc client is not started!");

    AbstractStub<?> service;
    ManagedChannel channel;
    Method stubInitializationMethod;
    try {
      if (!STUBS.containsKey(type)) {
        List<InstanceMetadata> instances = discovery.listAll(type.getName());
        if (instances == null || instances.isEmpty()) {
          log.error("No services are registered for '{}' in registry center '{}'!", type.getName(),
              discovery.getIdentifier());
          throw new RpcClientException("No services found!");
        }
        String serversConnString = getServiceProviderAddr(instances);
        if (!CHANNELS.containsKey(serversConnString)) {
          channel = establishChannel(instances);
          CHANNELS.putIfAbsent(serversConnString, channel);
        } else {
          channel = CHANNELS.get(serversConnString);
        }
        // Instantiate the generated gRPC class.
        Class<?> generatedGrpc =
            ClassHelper.forName(type.getName().substring(0, type.getName().indexOf('$')));
        if (type.getName().endsWith(GrpcConstants.CLIENT_STUB_SUFFIX_BLOCKING)) {
          stubInitializationMethod =
              generatedGrpc.getMethod(GrpcConstants.NEW_CLIENT_BLOCKING_STUB, Channel.class);
        } else if (type.getName().endsWith(GrpcConstants.CLIENT_STUB_SUFFIX_FUTURE)) {
          stubInitializationMethod =
              generatedGrpc.getMethod(GrpcConstants.NEW_CLIENT_FUTURE_STUB, Channel.class);
        } else {
          stubInitializationMethod =
              generatedGrpc.getMethod(GrpcConstants.NEW_CLIENT_ASYN_STUB, Channel.class);
        }
        Tuple<String, Method> tuple =
            new Tuple<String, Method>(serversConnString, stubInitializationMethod);
        STUBS.putIfAbsent(type, tuple);
      } else {
        channel = CHANNELS.get(STUBS.get(type).xobj);
        stubInitializationMethod = STUBS.get(type).yobj;
      }
      // instantiate the client stub according to the stub type
      service = (AbstractStub<?>) stubInitializationMethod.invoke(null, channel);
      //Customizes the CallOptions passed the deadline to interceptor
      if (timeout > 0) {
         service.withOption(CallerDeadlineGrpcClientInterceptor.DEADLINE_KEY, Integer.valueOf(timeout));
      }

    } catch (Exception ex) {
      log.error("Get remoting service '{}' from registry center '{}' error happend",
          type.getName(), discovery.getIdentifier(), ex);
      throw new RpcClientException(ex);
    }
    return service;
  }
  
  private ManagedChannel establishChannel(List<InstanceMetadata> instances)
      throws SSLException, IOException {
    NettyChannelBuilder builder =
        NettyChannelBuilder.forTarget(getServiceProviderAddr(instances));
    // 这里要注意下由于java版本的没有提供像go那样的可以指定域名
    // java版本源代码中把host传入作为证书域名
    // 域名是在证书生成的过程中自己输入的
    // TODO: to parameterize the serverHostOverride
    String serverHostOverride = "localhost";
    if (serverHostOverride != null) {
      // Force the hostname to match the cert the server uses.
      builder.overrideAuthority(serverHostOverride);
    }
    if (isUsedTls(instances)) {
      builder
          .sslContext(
              GrpcSslContexts.forClient().trustManager(SslUtils.loadCert("server.pem")).build())
          .negotiationType(NegotiationType.TLS);
    }
    // builder.nameResolverFactory(ZkNameResolverFactory.getInstance());
    return builder.intercept(new HeaderGrpcClientInterceptor(),
        new CallerDeadlineGrpcClientInterceptor()).build();
  }

  private boolean isUsedTls(List<InstanceMetadata> instances) {
    checkArgument(instances != null && instances.size() > 0,
        "Param cannot be null or cannot be a empty List!");
    InstanceMetadata instance = instances.get(0);
    return instance.isUseTls();
  }
  
  /**
   * Get service provider addresses.
   * @param instances
   * @return the addresses of service provider
   */
  private String getServiceProviderAddr(List<InstanceMetadata> instances) {
    checkArgument(instances != null, "Param cannot be null!");
    StringBuilder servers = new StringBuilder();
    servers.append(NameResolverProvider.providers().get(0).getDefaultScheme()).append("://");

    for (InstanceMetadata data : instances) {
      servers.append(data.getHostname()).append(':').append(data.getPort()).append('/');
    }
    return servers.toString();
  }

  /**
   * Load the service with lazy load style.
   * 
   * @return the instance of GrpcClient
   */
  public static GrpcClient getInstance() {
    return Loader.INSTANCE;
  }

  private static class Loader {
    private static final GrpcClient INSTANCE = new GrpcClient();

  }


}
