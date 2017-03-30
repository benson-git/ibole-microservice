package com.github.ibole.microservice.config.spring.annotation.test;

import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.config.rpc.client.RpcClient;

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
  public void initialize(ClientOptions clientOptions) {
   
    
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
