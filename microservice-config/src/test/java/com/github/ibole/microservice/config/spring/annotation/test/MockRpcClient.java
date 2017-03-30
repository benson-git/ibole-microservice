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

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.config.rpc.client.RpcClient#getRemotingService(java.lang.Class, java.lang.String, boolean, int)
   */
  @Override
  public DemoService getRemotingService(Class<? extends DemoService> type, String preferredZone,
      boolean usedTls, int timeout) {
    
    return new DemoServiceImpl();
  }

}
