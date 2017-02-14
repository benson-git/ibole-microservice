package io.ibole.microservice.registry.instance;


/*********************************************************************************************.
 * 
 * 
 * <p>版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
 * 
 * <p></p>
 *********************************************************************************************/


public class ServiceImplementationException extends Exception {

  public ServiceImplementationException(Exception e) {
    super(e);
  }
  
  public ServiceImplementationException(String errorMessage, Exception e) {
    super(errorMessage, e);
  }

  private static final long serialVersionUID = -6446789145075238310L;

}
