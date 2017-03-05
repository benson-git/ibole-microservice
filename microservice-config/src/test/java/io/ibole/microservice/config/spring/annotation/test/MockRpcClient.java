package io.ibole.microservice.config.spring.annotation.test;

import io.ibole.microservice.common.ServerIdentifier;
import io.ibole.microservice.config.rpc.client.RpcClient;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 * @param <T>
 *********************************************************************************************/


public class MockRpcClient implements RpcClient<DemoService> {

  @Override
  public void initialize(ServerIdentifier identifier) {
   
    
  }

  @Override
  public void start() {
    
    
  }

  @Override
  public void stop() {
   
    
  }

  @Override
  public DemoService getRemotingService(Class<? extends DemoService> type, int timeout) {
    
    return new DemoServiceImpl();
  }

}
