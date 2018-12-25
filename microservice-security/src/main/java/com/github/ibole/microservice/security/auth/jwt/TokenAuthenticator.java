package com.github.ibole.microservice.security.auth.jwt;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/

/**
 * 创建Token处理接口. 管理短有效期的access token和长生命期的refresh token. Refresh token并不能用于请求api.它是用来在access
 * token过期后刷新access token的一个标记.
 * 
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public interface TokenAuthenticator {

  /**
   * Create new access token base on the claim object.
   * 
   * @param claim JwtObject
   * @return generated token
   * @throws TokenHandlingException
   */
  String createAccessToken(JwtObject claim) throws TokenHandlingException;

  /**
   * Create new refresh token base on the claim object.
   * 
   * @param claim JwtObject
   * @return generated token
   * @throws TokenHandlingException
   */
  String createRefreshToken(JwtObject claim) throws TokenHandlingException;

  /**
   * Revoke a refresh token base on the claim object.
   */
  void revokeRefreshToken(String loginId);

  /**
   * Renew token base on the pri token.
   * 
   * @param refresh token the provided refresh to renew an access token
   * @param ttlSeconds the time to live
   * @return the new token
   * @throws TokenHandlingException
   */
  String renewAccessToken(String refreshToken, int ttlSeconds)
      throws TokenHandlingException;

  /**
   * 验证Access Token.
   */
  TokenStatus validAccessToken(String token, String clientId);

  /**
   * 验证Refresh Token.
   */
  TokenStatus validRefreshToken(String token, String clientId);
  
  /**
   * Parse token and convert it to JwtObject without validation.
   * @param token the token to parse
   * @throws TokenHandlingException error happen when parsing the token.
   * @return the instance of JwtObject
   */
  JwtObject parseTokenWithoutValidation(String token) throws TokenHandlingException;
  
}
