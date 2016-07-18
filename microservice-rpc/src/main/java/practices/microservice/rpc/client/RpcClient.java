package practices.microservice.rpc.client;

import practices.microservice.common.ServerIdentifier;

public interface RpcClient {
	
	void initialize(ServerIdentifier identifier);

	void start();
	
	void stop();
	
	 <T> T getRemotingService(Class<? extends T> type);
	 
}
