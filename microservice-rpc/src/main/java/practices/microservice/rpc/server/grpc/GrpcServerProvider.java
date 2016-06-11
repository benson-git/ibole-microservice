/**
 * 
 */
package practices.microservice.rpc.server.grpc;

import practices.microservice.rpc.server.RpcServer;
import practices.microservice.rpc.server.RpcServerProvider;
import practices.microservice.rpc.server.ServerBootstrap;

/**
 * Provider for {@link ServerBootstrap} instance. 
 * @author bwang
 *
 */
public class GrpcServerProvider extends RpcServerProvider {

	/* (non-Javadoc)
	 * @see practices.rpc.server.RpcServerProvider#isAvailable()
	 */
	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see practices.rpc.server.RpcServerProvider#priority()
	 */
	@Override
	protected int priority() {
		
		return 5;
	}

	@Override
	protected RpcServer createServer() {
		
		return new GrpcServer();
	}

}
