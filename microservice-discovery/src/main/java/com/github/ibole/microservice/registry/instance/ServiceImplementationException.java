package com.github.ibole.microservice.registry.instance;


/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>.
 * </p>
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
