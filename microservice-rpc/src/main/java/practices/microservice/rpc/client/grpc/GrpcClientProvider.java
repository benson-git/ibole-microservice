package practices.microservice.rpc.client.grpc;

import practices.microservice.rpc.client.RpcClient;
import practices.microservice.rpc.client.RpcClientProvider;

public class GrpcClientProvider extends RpcClientProvider {

	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	@Override
	protected int priority() {
		
		return 5;
	}

	@Override
	public RpcClient getRpcClient() {
		
		return GrpcClient.getInstance();
	}

}
