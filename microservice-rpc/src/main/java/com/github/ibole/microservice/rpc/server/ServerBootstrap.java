package com.github.ibole.microservice.rpc.server;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ibole.infrastructure.common.properties.ConfigurationBuilder;
import com.github.ibole.infrastructure.common.properties.ConfigurationHolder;
import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.common.io.NetworkUtil;
import com.github.ibole.microservice.common.utils.Constants;
import com.github.ibole.microservice.container.IocContainer;
import com.github.ibole.microservice.container.IocContainerProvider;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.RegisterEntry;
import com.github.ibole.microservice.registry.AbstractRegistryFactory;
import com.github.ibole.microservice.registry.RegistryFactory;
import com.github.ibole.microservice.registry.ServiceRegistry;
import com.github.ibole.microservice.registry.ServiceRegistryProvider;
import com.github.ibole.microservice.registry.instance.grpc.GrpcServiceDefinitionLoader;
import com.github.ibole.microservice.rpc.server.exception.RpcServerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


/**
 *  
 * Microservices Server launcher from the command.
 * TODO: move it to microservice-integration.
 * @author bwang
 *
 */
public class ServerBootstrap {

  private static Logger log = LoggerFactory.getLogger(ServerBootstrap.class);
 
  /**
   * The main application allowing this server to be launched from the command line.
   * @param args String[]
   */
  public static void main(String[] args) {
    
    try {  
      //load properties
      ConfigurationBuilder builder = new ConfigurationBuilder().defaults();
      builder.properties(ServerBootstrap.class.getResource(Constants.RPC_SERVER_PROPERTY_FILE)
          .toURI().toURL());
      builder.override(System.getProperties());
      Map<String, String> props = builder.build();
      ConfigurationHolder.set(props);
      //end of load properties
      //Parse parameters
      if (args != null && args.length > 0) {
        parseArgs(args);
      }
      //Print license
      echoLicense();
      //Boot server
      new ServerBootstrap().boot();
    } catch (Exception ex) {
      log.error("Server boot failed!", ex);
      throw new RpcServerException(ex);
    }
  }

  /**
   * Boot server.
   * 
   * @throws InterruptedException exception when boot error happen
   */
  public void boot() throws InterruptedException {

    log.info("Booting Microservices Server...");
    long times = System.currentTimeMillis();
    // Init IOC Container
    IocContainer iocContainer = IocContainerProvider.provider().createIocContainer();
    // Init RPC Server
    int port = Integer.parseInt(ConfigurationHolder.get().get(Constants.PROPERTY_SERVER_PORT));
    boolean useTls =
        Boolean.valueOf(ConfigurationHolder.get().get(Constants.PROPERTY_SERVER_USE_TLS));
    String registryHosts = ConfigurationHolder.get().get(Constants.PROPERTY_REGISTRY_HOSTS);
    //Create rpc server
    RpcServer rpcServer = RpcServerProvider.provider().createServer();
    rpcServer.configure(port, useTls);
    Runtime.getRuntime().addShutdownHook(new Thread("SHUTDOWN-RPC-Server") {
      @Override
      public void run() {
        try {
          log.info("RPC Server shutting down...");
          rpcServer.stop();
          log.info("RPC Server has been shut down on port {}", port);
        } catch (Exception e) {
          log.error("RPC Server shutting down error", e.getMessage(), e);
        }
        try {
          log.info("IOC Container shutting down...");
          iocContainer.stop();
          log.info("IOC Container has been shut down");
        } catch (Exception e) {
          log.error("IOC Container shutting down error", e.getMessage(), e);
        }
        log.info("Registry center shutting down...");
        AbstractRegistryFactory.destroyAll();
        log.info("Registry center has been shut down");
      }
    });
    // Register server interceptors.
    registerInterceptors(rpcServer);
    // Start IOC Container
    iocContainer.start();
    // Start RPC Server
    rpcServer.start();
    // Register services to register center (e.g. zookeeper)
    registerService(port, useTls, registryHosts);

    times = System.currentTimeMillis() - times;
    log.info("Microservices Server started on port {} in {} ms", port, times);

    rpcServer.blockUntilShutdown();
  }

  /**
   * @param rpcServer
   */
  private void registerInterceptors(RpcServer rpcServer) {
    List<RpcServerInterceptor> interceptors = RpcServerInterceptorProvider.getInterceptors();
    for(RpcServerInterceptor interceptor : interceptors){
      rpcServer.registerInterceptor(interceptor);
    }
  }

  private static void echoLicense() throws IOException {
    InputStream in = ServerBootstrap.class.getResourceAsStream("/License.txt");

    byte[] buffer = new byte[in.available()];
    in.read(buffer);
    in.close();

    log.info(new String(buffer));
  }

  /**
   * Register services to registry center. Another good example to do the gRPC service registry:
   * {@link <a href=
   * "https://github.com/LogNet/grpc-spring-boot-starter/blob/master/grpc-spring-boot-starter/src/main/java/org/lognet/springboot/grpc/GRpcServerRunner.java">
   * grpc-spring-boot-starter</a>}
   * 
   * @param params the params to set
   */
  private void registerService(int port, boolean useTls, String hosts) {
    if (Strings.isNullOrEmpty(hosts)) {
      log.warn("No registry servers is specified in the parameters, skill the registry service!");
      return;
    }
    String rpcServer = ConfigurationHolder.get().get(Constants.PROPERTY_SERVER_HOSTNAME);
    String registryBaseKey = ConfigurationHolder.get().get(Constants.PROPERTY_REGISTRY_ROOT_PATH);
    //If has not specify the rpc server host, we just get local default host.
    if (Strings.isNullOrEmpty(rpcServer)) {
      rpcServer = NetworkUtil.getDefaultLocalHost();
    }
    ServerIdentifier identifier = new ServerIdentifier(registryBaseKey, hosts);
    RegistryFactory<ServiceRegistry<HostMetadata>> registryFactory =
        ServiceRegistryProvider.provider().getRegistryFactory();
    ServiceRegistry<HostMetadata> serviceRegistry =
        registryFactory.getServiceRegistry(identifier);
    serviceRegistry.start();

    //TODO should not be specific GrpcServiceDefinitionLoader
    List<String> serviceStubs = GrpcServiceDefinitionLoader.load().getServiceStubList();
    RegisterEntry entry = new RegisterEntry();
    HostMetadata metadata;
    for (String service : serviceStubs) {
      metadata = new HostMetadata(rpcServer, port, useTls);
      entry.setServiceName(ServerIdentifier.BASE_KEY_PREFIX);
      entry.setServiceContract(service);
      // TODO: add useful service description for the service consumer
      entry.setDescription(service);
      entry.setLastUpdated(Calendar.getInstance().getTime());
      entry.setHostMetadata(metadata);
      serviceRegistry.register(entry);
    }
    log.info("Register service is finished, total {} services are registered.",
        serviceStubs.size());
  }

  private static void parseArgs(String[] args) {
    boolean usage = false;
    for (String arg : args) {
      if (!arg.startsWith("--")) {
        log.error("All arguments must start with '--': {}", arg);
        usage = true;
        break;
      }
      String[] parts = arg.substring(2).split("=", 2);
      String key = parts[0];
      if ("help".equals(key)) {
        usage = true;
        break;
      }
      if (parts.length != 2) {
        log.error("All arguments must be of the form --arg=value");
        usage = true;
        break;
      }
      String value = parts[1];
      if ("port".equals(key)) {
        ConfigurationHolder.get().put(Constants.PROPERTY_SERVER_PORT, value);
      } else if ("use_tls".equals(key)) {
        ConfigurationHolder.get().put(Constants.PROPERTY_SERVER_USE_TLS, value);
      } else if ("reg_servers".equals(key)) {
        ConfigurationHolder.get().put(Constants.PROPERTY_REGISTRY_HOSTS, value);
      } else {
        log.error("Unknown argument: {}", key);
        usage = true;
        break;
      }
    }
    if (usage) {
      log.info("Usage: [ARGS...]" + "\n" + "\n  --port=PORT Port to connect to. Default "
          + Constants.RpcServerEnum.DEFAULT_CONFIG.getPort()
          + "\n  --use_tls=true|false  Whether to use TLS. Default "
          + Constants.RpcServerEnum.DEFAULT_CONFIG.isUseTls()
          + "\n  --reg_servers=SERVER LIST  Where to connect to registry center. Default is skip."
          + "\n Above all parameters also can be configured in /resources/server.properties.");
      
      System.exit(1);
    }
  }
}
