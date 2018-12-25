package com.github.ibole.microservice.security.auth.jwt;

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
public class TokenSignatureException extends Exception {


  /**
   * 
   */
  private static final long serialVersionUID = -4232725203751990825L;

  public TokenSignatureException(String message)
  {
      super(message);
  }

  public TokenSignatureException(String message, Throwable cause)
  {
      super(message, cause);
  }
  
  public TokenSignatureException(Throwable cause) {
    super(cause);
}
}
