package practices.microservice.rpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.common.ServerIdentifier.ServiceType;
import practices.microservice.common.io.NetworkUtil;
import practices.microservice.common.utils.Constants;
import practices.microservice.container.IocContainer;
import practices.microservice.container.IocContainerProvider;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.RegisterEntry;
import practices.microservice.registry.AbstractRegistryFactory;
import practices.microservice.registry.RegistryFactory;
import practices.microservice.registry.ServiceRegistry;
import practices.microservice.registry.ServiceRegistryProvider;
import practices.microservice.registry.instance.grpc.GrpcServiceDefinitionLoader;

import com.google.common.base.Strings;


/**
 * Microservices Server launcher from the command.
 * 
 * @author bwang
 *
 */
public class ServerBootstrap {

	private static Logger log = LoggerFactory.getLogger(ServerBootstrap.class);
	/**
	 * The main application allowing this server to be launched from the command
	 * line.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String[] params = parseArgs(args);
        new ServerBootstrap().boot(params);
	}
	
	public void boot(final String[] params) throws Exception{
		
		log.info("Booting Microservices Server...");
		long times = System.currentTimeMillis();	
		echoLicense();
		//Init IOC Container
		IocContainer iocContainer = IocContainerProvider.provider().createIocContainer();
		//Init RPC Server
		RpcServer rpcServer = RpcServerProvider.provider().createServer();
		rpcServer.configure(Integer.parseInt(params[0]), Boolean.valueOf(params[1]));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					log.info("RPC Server shutting down...");
					rpcServer.stop();
					log.info("RPC Server has been shut down on port {}", params[0]);
				} catch (Exception e) {				
					log.error("RPC Server shutting down error", e.getMessage(), e);
				}
				try {
					log.info("IOC Container shutting down...");
					log.info("IOC Container has been shut down");
					iocContainer.stop();
				} catch (Exception e) {				
					log.error("IOC Container shutting down error", e.getMessage(), e);
				}
				log.info("Registry center shutting down...");
				AbstractRegistryFactory.destroyAll();
				log.info("Registry center has been shut down");
			}
		});
		//Start IOC Container
		iocContainer.start();
		//Start RPC Server
		rpcServer.start();
		//Register services to register center
		registerService(params);
		
		times = System.currentTimeMillis() - times;
		log.info("Microservices Server started on port {} in {} ms", params[0], times);

		rpcServer.blockUntilShutdown();
	}
	
	private static void echoLicense() throws IOException {
		InputStream in = ServerBootstrap.class.getResourceAsStream("/License.txt");

		byte[] buffer = new byte[in.available()];
		in.read(buffer);
		in.close();

		log.info(new String(buffer));
	}
	/**
	 * Register services to registry center.
	 * Another good example to do the gRPC service registry:
	 * {@link <a href="https://github.com/LogNet/grpc-spring-boot-starter/blob/master/grpc-spring-boot-starter/src/main/java/org/lognet/springboot/grpc/GRpcServerRunner.java">grpc-spring-boot-starter</a>}
	 * @param params
	 */
	private void registerService(String[] params) {
		if(params.length < 3 || Strings.isNullOrEmpty(params[2])){
			log.warn("No registry servers is specified in the parameters, skill the registry service!");
			return;
		}
		ServerIdentifier identifier = new ServerIdentifier(ServiceType.RPC, params[2]);
		RegistryFactory<ServiceRegistry<InstanceMetadata>> registryFactory = ServiceRegistryProvider.provider().getRegistryFactory();
		ServiceRegistry<InstanceMetadata> serviceRegistry = registryFactory.getServiceRegistry(identifier);
		serviceRegistry.start();
		
		List<String> serviceStubs = GrpcServiceDefinitionLoader.load().getServiceStubList();
		RegisterEntry entry = new RegisterEntry();
		InstanceMetadata metadata;
		for(String service : serviceStubs){
			metadata = new InstanceMetadata(UUID.randomUUID(), NetworkUtil.getDefaultLocalHost(), Integer.valueOf(params[0]), Boolean.valueOf(params[1]));
			entry.setServiceName(ServerIdentifier.BASE_KEY);
			entry.setServiceContract(service);
			//TODO: add useful service description for the service consumer 
			entry.setDescription(service);
			entry.setServiceType(ServiceType.RPC);
			entry.setLastUpdated(Calendar.getInstance().getTime());
			entry.setInstanceMetadata(metadata);
			serviceRegistry.register(entry);
		}	
		log.info("Register service is finished, total {} services are registered.", serviceStubs.size());
    }
		
	private static String[] parseArgs(String[] args) {
		boolean usage = false;
		String[] params = new String[3];
		for (String arg : args) {
			if (!arg.startsWith("--")) {
				//System.err.println("All arguments must start with '--': " + arg);
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
//				System.err
//						.println("All arguments must be of the form --arg=value");
				log.error("All arguments must be of the form --arg=value");
				usage = true;
				break;
			}
			String value = parts[1];
			if ("port".equals(key)) {
				params[0] = value;
			} else if ("use_tls".equals(key)) {
				params[1] = value;
			} else if ("reg_servers".equals(key)) {
				params[2] = value;
			} 
			else {
//				System.err.println("Unknown argument: " + key);
				log.error("Unknown argument: {}", key);
				usage = true;
				break;
			}
		}
		if (usage) {
			log.info("Usage: [ARGS...]" + "\n"
					+ "\n  --port=PORT Port to connect to. Default "
					+ Constants.RpcServerEnum.DEFAULT_CONFIG.getPort()
					+ "\n  --use_tls=true|false  Whether to use TLS. Default "
					+ Constants.RpcServerEnum.DEFAULT_CONFIG.isUseTls()
					+ "\n  --reg_servers=SERVER LIST  Where to connect to registry center. Default is skip.");		
			System.exit(1);
		}
		
		return params;
	}
}
