package io.ibole.microservice.rpc.client.exception;



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
