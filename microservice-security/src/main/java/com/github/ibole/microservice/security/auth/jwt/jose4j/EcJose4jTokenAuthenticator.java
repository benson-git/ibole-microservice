/*
 * Copyright 2016-2017 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.ibole.microservice.security.auth.jwt.jose4j;

import com.github.ibole.microservice.common.utils.Constants;
import com.github.ibole.microservice.security.auth.jwt.BaseTokenAuthenticator;
import com.github.ibole.microservice.security.auth.jwt.BaseTokenValidationCallback;
import com.github.ibole.microservice.security.auth.jwt.GeneralJwtException;
import com.github.ibole.microservice.security.auth.jwt.JwtConstant;
import com.github.ibole.microservice.security.auth.jwt.JwtObject;
import com.github.ibole.microservice.security.auth.jwt.RefreshTokenNotFoundException;
import com.github.ibole.microservice.security.auth.jwt.TokenHandlingException;
import com.github.ibole.microservice.security.auth.jwt.TokenStatus;
import com.github.ibole.microservice.security.auth.jwt.TokenValidationCallback;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey.OutputControlLevel;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

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
 * Token lifecycle management (create/renew/validate/revoke).
 * 
 * @author bwang (chikaiwang@hotmail.com)
 * 
 */
public class EcJose4jTokenAuthenticator extends BaseTokenAuthenticator {

  private PublicJsonWebKey ecJsonWebKey;

  public EcJose4jTokenAuthenticator() {
    try {
      ecJsonWebKey = JoseUtils
          .toJsonWebKey(getClass().getResource(Constants.SENDER_JWK_PATH).toURI().getPath());
      logger.debug("EC Keys: {}", ecJsonWebKey.toJson(OutputControlLevel.INCLUDE_PRIVATE));
    } catch (URISyntaxException ex) {
       throw new GeneralJwtException(ex);
    }
  }

  /**
   * Create Access Token.
   * 
   */
  @Override
  public String createAccessToken(JwtObject claimObj) throws TokenHandlingException {
    Preconditions.checkArgument(claimObj != null, "Parameter claimObj cannot be null");
    final Stopwatch stopwatch = Stopwatch.createStarted();
    String token = null;
    try {
      if (!Constants.ANONYMOUS_ID.equalsIgnoreCase(claimObj.getLoginId())
          && !getCacheService().has(getRefreshTokenKey(claimObj.getLoginId()))) {
        throw new RefreshTokenNotFoundException("Refresh token not found.");
      }
      token = JoseUtils.createJwtWithECKey(claimObj, (EllipticCurveJsonWebKey) ecJsonWebKey);
      getCacheService().set(getRefreshTokenKey(claimObj.getLoginId()), Constants.ACCESS_TOKEN,
          token);
    } catch (JoseException ex) {
      logger.error("Error happened when generating the jwt token.", ex);
      throw new TokenHandlingException(ex);
    }
    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    logger.debug("Create access token elapsed time: {} ms", elapsedString);
    return token;
  }

  /**
   * Create Refresh Token.
   */
  @Override
  public String createRefreshToken(JwtObject claimObj) throws TokenHandlingException {
    Preconditions.checkArgument(claimObj != null, "Parameter claimObj cannot be null");
    final Stopwatch stopwatch = Stopwatch.createStarted();
    String token = null;
    try {

      token = JoseUtils.createJwtWithECKey(claimObj, (EllipticCurveJsonWebKey) ecJsonWebKey);
      getCacheService().set(getRefreshTokenKey(claimObj.getLoginId()), Constants.REFRESH_TOKEN,
          token);
      getCacheService().set(getRefreshTokenKey(claimObj.getLoginId()), Constants.CLIENT_ID,
          claimObj.getClientId());
      getCacheService()
          .expire(getRefreshTokenKey(claimObj.getLoginId()), claimObj.getTtlSeconds());

    } catch (JoseException ex) {
      logger.error("Error happened when generating the jwt token.", ex);
      throw new TokenHandlingException(ex);
    }
    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    logger.debug("Create refresh token elapsed time: {} ms", elapsedString);
    return token;
  }

  /**
   * 验证Access Token的合法性,判断是否被篡改或者盗用.
   */
  @Override
  public TokenStatus validAccessToken(String token, String clientId) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "Token cannot be null");

    final Stopwatch stopwatch = Stopwatch.createStarted();

    TokenValidationCallback<JwtClaims> validationCallback =
        new BaseTokenValidationCallback<JwtClaims>() {
          @Override
          public void onInValid(final JwtClaims jwtClaims) {
            super.onInValid(jwtClaims);
            // TODO: calculates the number of consecutive failures to validate token. lock the
            // account
            // if the failed number over than the limitation.
          }

          @Override
          public void onExpired(final JwtClaims jwtClaims) {
            super.onExpired(jwtClaims);
            String loginId = String.valueOf(jwtClaims.getClaimValue(JwtConstant.LOGIN_ID));
            // anonymous access don't have the refresh token
            if (Constants.ANONYMOUS_ID.equalsIgnoreCase(loginId)) {
              return;
            }
            // As it is expensive to frequently check the refresh token (from redis),
            // here we just do it when the access token is expired.
            String refreshToken =
                getCacheService().get(getRefreshTokenKey(loginId), Constants.REFRESH_TOKEN);
            // check if the refresh token is expired
            if (Strings.isNullOrEmpty(refreshToken)) {
              setTokenStatus(TokenStatus.INVALID);
            } else {
              // if the same login id logon in different client.
              // Check if the both client id and login id are match with the provided token.
              String previousClientId =
                  getCacheService().get(getRefreshTokenKey(loginId), Constants.CLIENT_ID);
              if (!clientId.equals(previousClientId)) {
                setTokenStatus(TokenStatus.INVALID);
              }
            }
          }

          @Override
          public void onError(final JwtClaims jwtClaims) {
            super.onError(jwtClaims);
            // TODO: calculates the number of consecutive failures to validate the same token. lock
            // the account
            // if the failed number over than the limitation.
            logger.debug("Valid access token error happened for account '{}' from client '{}'",
                jwtClaims != null ? jwtClaims.getClaimValue(JwtConstant.LOGIN_ID) : "UNKNOWN",
                clientId);
          }
        };

    // validate the token signature.
    JoseUtils.validateToken(token, clientId, (PublicJsonWebKey) ecJsonWebKey, validationCallback);

    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    logger.debug("Validate token elapsed time: {} ms", elapsedString);

    return validationCallback.getTokenStatus();
  }

  /**
   * 验证Refresh Token的合法性,判断是否被篡改或者盗用.
   */
  @Override
  public TokenStatus validRefreshToken(String token, String clientId) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "Token cannot be null");

    final Stopwatch stopwatch = Stopwatch.createStarted();

    TokenValidationCallback<JwtClaims> validationCallback =
        new BaseTokenValidationCallback<JwtClaims>() {
          @Override
          public void onInValid(final JwtClaims jwtClaims) {
            super.onInValid(jwtClaims);
            // TODO: calculates the number of consecutive failures to validate the same token. lock
            // the account
            // if the failed number over than the limitation.
            // if (jwtClaims != null) {
            // revokeRefreshToken(String.valueOf(jwtClaims.getClaimValue(JwtConstant.LOGIN_ID)));
            // }
          }

          @Override
          public void onValiated(final JwtClaims jwtClaims) {
            super.onValiated(jwtClaims);
            String loginId = String.valueOf(jwtClaims.getClaimValue(JwtConstant.LOGIN_ID));
            String refreshToken =
                getCacheService().get(getRefreshTokenKey(loginId), Constants.REFRESH_TOKEN);
            // check if the refresh token is expired
            if (Strings.isNullOrEmpty(refreshToken)) {
              setTokenStatus(TokenStatus.EXPIRED);
              return;
            } else {
              // if the same login id logon in different client.
              // Check if the both client id and login id are match with the provided token.
              String previousClientId =
                  getCacheService().get(getRefreshTokenKey(loginId), Constants.CLIENT_ID);
              if (!clientId.equals(previousClientId)) {
                setTokenStatus(TokenStatus.INVALID);
                return;
              }
              
              if(!token.equals(refreshToken)) {
                setTokenStatus(TokenStatus.INVALID);
                return;
              }
            }
            
          }

          @Override
          public void onError(final JwtClaims jwtClaims) {
            super.onError(jwtClaims);
            // TODO: count error times per client account and then lock account if the error times
            // over than the limitation.
            logger.debug("Valid refresh token error happened for account '{}' from client '{}'",
                jwtClaims != null ? jwtClaims.getClaimValue(JwtConstant.LOGIN_ID) : "UNKNOWN",
                clientId);
          }
        };

    // validate the token signature.
    JoseUtils.validateToken(token, clientId, (PublicJsonWebKey) ecJsonWebKey, validationCallback);

    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    logger.debug("Validate token elapsed time: {} ms", elapsedString);

    return validationCallback.getTokenStatus();
  }

  @Override
  public String renewAccessToken(String refreshToken, int ttlSeconds)
      throws TokenHandlingException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(refreshToken), "Token cannot be null");
    final Stopwatch stopwatch = Stopwatch.createStarted();
    
    String newToken;
    JwtObject jwtObj = JoseUtils.claimsOfTokenWithoutValidation(refreshToken);
    jwtObj.setTtlSeconds(ttlSeconds);
    newToken = createAccessToken(jwtObj);
    
    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    logger.debug("Renew token elapsed time: {} ms", elapsedString);
    return newToken;
  }  

  @Override
  public void revokeRefreshToken(String loginId) {
    getCacheService().remove(getRefreshTokenKey(loginId));
  }
  
  @Override
  public JwtObject parseTokenWithoutValidation(String token) throws TokenHandlingException {
    return JoseUtils.claimsOfTokenWithoutValidation(token);
  }

  private String getRefreshTokenKey(String loginId) {
    return Constants.REFRESH_TOKEN_KEY_PREFIX + loginId;
  }

}
