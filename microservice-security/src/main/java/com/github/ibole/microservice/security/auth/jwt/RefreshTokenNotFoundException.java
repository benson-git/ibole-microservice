/**
 * 
 */
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
public class RefreshTokenNotFoundException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public RefreshTokenNotFoundException(String message)
  {
      super(message);
  }

  public RefreshTokenNotFoundException(String message, Throwable cause)
  {
      super(message, cause);
  }

  public RefreshTokenNotFoundException(Throwable cause) {
      super(cause);
  }
}
