package com.github.ibole.microservice.rpc.test;



import com.github.ibole.microservice.container.IocContainer;
import com.github.ibole.microservice.container.IocContainerProvider;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public class ServiceTestResource extends ExternalResource {
  
  protected final static Logger logger = LoggerFactory.getLogger(ServiceTestResource.class.getName());

  public static RpcTestServer server = null;
  public IocContainer iocContainer = null;
  public static GrpcTestClient client;
  
  public static boolean testSuite;
  
  private ServiceTestResource() {
    //do nothing.
  }
  
  private ServiceTestResource(boolean isTestSuite) {
    testSuite = isTestSuite;
  }

  public static ServiceTestResource newInstance(boolean isTestSuite){
    return new ServiceTestResource(isTestSuite);
  }

  @Override
  protected void before() throws Throwable{
    // Init IOC Container
    iocContainer = IocContainerProvider.provider().createIocContainer();
    iocContainer.start();

    server = new RpcTestServer();
    server.configure(-1, false);
    server.registerInterceptors();
    server.start();

    client = new GrpcTestClient();
    client.initialize(server.getServer().getPort());
    
    logger.info("Rpc test server started!");
  };

  @Override
  protected void after(){
    try {
      server.stop();
      client.shutdown();
      iocContainer.stop();
      server = null;
      iocContainer = null;
    } catch (InterruptedException ex) {
       logger.error("Rpc test server stopped error!", ex);
    }

    logger.info("Rpc test server stopped!");
  };
 
}
