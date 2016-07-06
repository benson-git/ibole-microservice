package practices.microservice.demo.helloworld.client;

import io.grpc.StatusRuntimeException;

import java.util.logging.Level;
import java.util.logging.Logger;

import practices.microservice.demo.protos.GreeterGrpc.GreeterBlockingStub;
import practices.microservice.demo.protos.Helloworld.HelloReply;
import practices.microservice.demo.protos.Helloworld.HelloRequest;
import practices.microservice.rpc.client.grpc.RpcClientHelper;

public class ClientExample {
	
	private static final Logger logger = Logger
			.getLogger(ClientExample.class.getName());

	public static void main(String[] args) {

		RpcClientHelper.getInstance().start();
		
		GreeterBlockingStub blockingStub = RpcClientHelper.getInstance().getRemotingService(GreeterBlockingStub.class);
		
		String name = "world";
		
		logger.info("Will try to greet " + name + " ...");
		HelloRequest request = HelloRequest.newBuilder().setName(name).build();
		HelloReply response;
		try {
			response = blockingStub.sayHello(request);
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return;
		}  
		logger.info("Greeting: " + response.getMessage());
		
		RpcClientHelper.getInstance().stop();
	}

}
