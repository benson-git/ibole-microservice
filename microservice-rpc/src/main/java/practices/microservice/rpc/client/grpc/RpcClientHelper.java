package practices.microservice.rpc.client.grpc;

import static com.google.common.base.Preconditions.checkArgument;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.common.ServerIdentifier.ServiceType;
import practices.microservice.common.utils.SSLUtils;
import practices.microservice.discovery.DiscoveryFactory;
import practices.microservice.discovery.InstanceMetadata;
import practices.microservice.discovery.ServiceDiscovery;
import practices.microservice.discovery.ServiceDiscoveryProvider;

import com.google.common.collect.Maps;

/**
 * RPC client helper.
 * 
 * @author bwang
 *
 */
public class RpcClientHelper {

	private static Logger log = LoggerFactory.getLogger(RpcClientHelper.class);
	private static ServiceDiscovery<InstanceMetadata> discovery = null;
	// Store the mapping between service class and rpc servers connection key
	private static final Map<Class<?>, String> SERVICES = Maps.newConcurrentMap();
	// Store the mapping between rpc servers connection key and channel instance
	private static final Map<String, ManagedChannel> CHANNELS = Maps.newConcurrentMap();

	private RpcClientHelper() {
		// do nothing.
	}

	public void start() {

		ServerIdentifier identifier = new ServerIdentifier(ServiceType.RPC,
				"localhost:2181,localhost:2182,localhost:2183");
		
		DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> factory = ServiceDiscoveryProvider.provider()
				.getDiscoveryFactory();
		discovery = factory.getServiceDiscovery(identifier);
		discovery.start();
		
	    if (log.isInfoEnabled()) {
	    	log.info("Rpc client is started.");
	    }
	}
	
	public void stop() {
		try {
			discovery.destroy();
			for (String key : CHANNELS.keySet()) {
				CHANNELS.get(key).shutdown()
						.awaitTermination(5, TimeUnit.SECONDS);
			}
			CHANNELS.clear();
			SERVICES.clear();
		} catch (Exception e) {
			log.error("Rpc client stop error happened", e);
			throw new RuntimeException();
		}
		
	    if (log.isInfoEnabled()) {
	    	log.info("Rpc client is stopped.");
	    }
	}

	public <T> T getRemotingService(Class<? extends T> type) {
		checkArgument(type != null, "Param cannot be null!");

		T service = null;
		ManagedChannel channel;
		try {
			Constructor<? extends T> constructor = type.getDeclaredConstructor(Channel.class);
			constructor.setAccessible(true);
     
			if (!SERVICES.containsKey(type)) {
				List<InstanceMetadata> instances = discovery.listAll(type.getName());
				if(instances == null || instances.size() == 0){
				    throw new RuntimeException("No services are registered for '"+type.getName()+"' in registry center!");
				}
				String rpcServersConnectionString = getServerConnectionString(instances);
				if (!CHANNELS.containsKey(rpcServersConnectionString)){
					channel = establishChannel(instances);
					CHANNELS.putIfAbsent(rpcServersConnectionString, channel);
				}
				else{
					channel = CHANNELS.get(rpcServersConnectionString);
				}
				SERVICES.putIfAbsent(type, rpcServersConnectionString);
			} else {
				channel = CHANNELS.get(SERVICES.get(type));
			}
			service = constructor.newInstance(channel);
		} catch (Exception e) {
			log.error("Get remoting service '{}' error happend", type.getName(), e);
			throw new RuntimeException();
		}

		return service;
	}

	private  ManagedChannel establishChannel(List<InstanceMetadata> instances) throws SSLException, IOException {
		NettyChannelBuilder builder = NettyChannelBuilder.forTarget(getServerConnectionString(instances));
		//这里要注意下由于java版本的没有提供像go那样的可以指定域名
	    // java版本源代码中把host传入作为证书域名
	    // 域名是在证书生成的过程中自己输入的
		//TODO: to parameterize the serverHostOverride
		String serverHostOverride = "localhost";
		if (serverHostOverride != null) {
            // Force the hostname to match the cert the server uses.
        	builder.overrideAuthority(serverHostOverride);
        }
		if (isUsedTls(instances)) {
			builder.sslContext(GrpcSslContexts.forClient().trustManager(SSLUtils.loadCert("server.pem")).build())
					.negotiationType(NegotiationType.TLS);
		}
		builder.nameResolverFactory(ZkNameResolverFactory.getInstance());
		return builder.build();
	}

	private  boolean isUsedTls(List<InstanceMetadata> instances) {
		checkArgument(instances != null && instances.size() > 0, "Param cannot be null or cannot be a empty List!");
		InstanceMetadata instance = instances.get(0);
		return instance.isUseTls();
	}

	private  String getServerConnectionString(List<InstanceMetadata> instances) {
		checkArgument(instances != null, "Param cannot be null!");
		StringBuilder servers = new StringBuilder();
		servers.append(ZkNameResolverFactory.SCHEME).append("://");

		for (InstanceMetadata data : instances) {
			servers.append(data.getHostname()).append(':').append(data.getPort()).append('/');
		}
		//servers = servers.deleteCharAt(servers.length() - 1);
		return servers.toString();
	}

	/**
	 * Load the service with lazy load style.
	 * 
	 * @return the instance of RpcClientHelper
	 */
	public static RpcClientHelper getInstance() {
		return Loader.INSTANCE;
	}

	private static class Loader {
		private final static RpcClientHelper INSTANCE = new RpcClientHelper();

	}
}
