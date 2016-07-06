package practices.microservice.rpc.server.grpc;

import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.utils.Constants;
import practices.microservice.common.utils.SSLUtils;
import practices.microservice.registry.instance.grpc.GrpcServiceDefinitionLoader;
import practices.microservice.rpc.server.RpcServer;


/**
 * Server that manages startup/shutdown of all services.
 * 
 * @author bwang
 *
 */
public class GrpcServer implements RpcServer {

	private final static Logger LOG = LoggerFactory.getLogger(GrpcServer.class);

	private boolean useTls = Constants.RpcServerEnum.DEFAULT_CONFIG.isUseTls();

	private int port = Constants.RpcServerEnum.DEFAULT_CONFIG.getPort();

	private Server server;

	public void configure(int pPort, boolean pUseTls) {
		port = pPort;
		useTls = pUseTls;
	}

	public void start() throws IOException {

		SslContext sslContext = null;
		NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
		if (useTls) {
			sslContext = GrpcSslContexts.forServer(
					SSLUtils.loadCert("server.pem"),
					SSLUtils.loadCert("server.key")).build();
			serverBuilder.sslContext(sslContext);
		}
		serverBuilder = bindService(serverBuilder);
		server = serverBuilder.flowControlWindow(65 * 1024)
				.build().start();
	}
    /**
     * Load gRPC specified service definition and then add to gRPC registry.
     * 
     * @param serverBuilder
     * @return the instance of NettyServerBuilder
     */
	public NettyServerBuilder bindService(NettyServerBuilder serverBuilder)
	{
		List<ServerServiceDefinition> services = GrpcServiceDefinitionLoader.load().getServiceList();
		for(ServerServiceDefinition service : services){
			serverBuilder.addService(service);
		}
		return serverBuilder;
	}
	
	
	public void stop() throws InterruptedException {
		server.shutdownNow();
		if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
			LOG.error("Timed out waiting for server shutdown");
		}
		// MoreExecutors
		// .shutdownAndAwaitTermination(executor, 5, TimeUnit.SECONDS);
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	public void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
}
