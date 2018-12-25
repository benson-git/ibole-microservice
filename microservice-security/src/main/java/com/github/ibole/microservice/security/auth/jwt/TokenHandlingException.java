package com.github.ibole.microservice.security.auth.jwt;

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
public class TokenHandlingException extends Exception {
  
  /**
   * 
   */
  private static final long serialVersionUID = 4704645465359901261L;

  public TokenHandlingException(String message)
  {
      super(message);
  }

  public TokenHandlingException(String message, Throwable cause)
  {
      super(message, cause);
  }

  public TokenHandlingException(Throwable cause) {
      super(cause);
  }
}
