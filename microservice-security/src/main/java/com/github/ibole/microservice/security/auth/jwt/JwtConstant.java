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
public final class JwtConstant {
  //密钥 id
  public static final String KID_RSA = "RSA_USING_SHA256";
  
  public static final String KID_ES256_ECDSA = "ES256_ECDSA";
  
  public static final String CLIENT_ID = "clientId";
  
  public static final String LOGIN_ID = "loginId";
  
  public static final String ROLE_ID = "roles";
  
  public static final int TTL_SECONDS = 1800;
  
  private JwtConstant(){};

}
