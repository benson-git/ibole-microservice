package practices.microservice.demo.server;

import practices.microservice.rpc.server.ServerBootstrap;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) throws Exception {

//		ApplicationContext ctx = new ClassPathXmlApplicationContext(
//				"classpath*:META-INF/spring/*.xml");
//
//		// TestObject to = (TestObject) ctx.getBean("testObject");
//		// System.out.println(to);
//		//
//		GreeterGrpc.Greeter greeterImpl1 = ctx.getBean(GreeterGrpc.Greeter.class);
//		GreeterGrpc.Greeter greeterImpl2 = ctx.getBean(GreeterGrpc.AbstractGreeter.class);
//		GreeterGrpc.Greeter greeterImpl3 = ctx.getBean(GreeterGrpc.GreeterImplBase.class);
//		System.out.println(greeterImpl1.hashCode());


		String[] args1 = new String[]{"--port=8443","--use_tls=true","--reg_servers=localhost:2181,localhost:2182,localhost:2183"};
		
		ServerBootstrap.main(args1);

	}
}
  