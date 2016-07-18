package practices.microservice.demo.helloworld.client;

import io.grpc.Status;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import practices.microservice.demo.protos.GreeterGrpc.GreeterBlockingStub;
import practices.microservice.demo.protos.Helloworld.HelloReply;
import practices.microservice.demo.protos.Helloworld.HelloRequest;
import practices.microservice.rpc.client.RpcClientProvider;

public class ClientExample {

	private static final Logger logger = Logger.getLogger(ClientExample.class
			.getName());
	
	private static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";
	
	private static ClassPathXmlApplicationContext context;

	public static void main(String[] args) {
		try {
			HelloReply response = null;

			context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
			context.start();

			GreeterBlockingStub blockingStub = context.getBean(GreeterBlockingStub.class);

			String name = "world";

			logger.info("Will try to greet " + name + " ...");
			HelloRequest request = HelloRequest.newBuilder().setName(name)
					.build();

			response = blockingStub.sayHello(request);

			logger.info("Greeting: " + response.getMessage());

		} catch (Exception e) {
			Status status = Status.fromThrowable(e);
			logger.log(Level.WARNING, "RPC failed: {0}", e);
			return;
		} finally {
			context.stop();
		}
	}

}
