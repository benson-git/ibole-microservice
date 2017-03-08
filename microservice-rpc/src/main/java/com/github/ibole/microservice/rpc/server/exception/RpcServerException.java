package com.github.ibole.microservice.rpc.server.exception;



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
public class RpcServerException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = -6224578318211913633L;

  public RpcServerException(Throwable cause) {
    super(cause);
  }
  
  public RpcServerException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
  }

  public RpcServerException(String errorMessage) {
    super(errorMessage);
  }
}
