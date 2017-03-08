/**
 * 
 */
package com.github.ibole.microservice.config.spring.annotation.test;

import com.github.ibole.microservice.config.rpc.client.RpcClient;
import com.github.ibole.microservice.config.rpc.client.RpcClientProvider;

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
public class MockClientProvider extends RpcClientProvider {

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.config.rpc.client.RpcClientProvider#isAvailable()
   */
  @Override
  protected boolean isAvailable() {
   
    return true;
  }

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.config.rpc.client.RpcClientProvider#priority()
   */
  @Override
  protected int priority() {
   
    return 0;
  }

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.config.rpc.client.RpcClientProvider#getRpcClient()
   */

  @Override
  public RpcClient<DemoService> getRpcClient() {
    
    return new MockRpcClient();
  }

}
