package practices.microservice.demo.server;

import practices.microservice.rpc.server.ServerBootstrap;

public class MainAPP {
	public static void main(String[] args) throws Exception {

//		ApplicationContext ctx = new ClassPathXmlApplicationContext(
//				"beans-annotation.xml");
//
//		// TestObject to = (TestObject) ctx.getBean("testObject");
//		// System.out.println(to);
//		//
//		GreeterGrpc.Greeter greeterImpl = ctx.getBean(GreeterGrpc.Greeter.class);
//		System.out.println(greeterImpl.hashCode());
		

		String[] args1 = new String[]{"--port=8443","--use_tls=true"};
		
		ServerBootstrap.main(args1);

	}
}