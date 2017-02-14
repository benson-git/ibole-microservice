package io.ibole.microservice.rpc.server.exception;



/*********************************************************************************************.
 * 
 * 
 * <p>版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
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
