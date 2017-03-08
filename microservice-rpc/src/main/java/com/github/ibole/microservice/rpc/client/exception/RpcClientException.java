package com.github.ibole.microservice.rpc.client.exception;



/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class RpcClientException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 7058435344448074545L;

  public RpcClientException(Throwable cause) {
    super(cause);
  }
  
  public RpcClientException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
  }

  public RpcClientException(String errorMessage) {
    super(errorMessage);
  }

}
