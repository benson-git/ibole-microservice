/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author bwang
 *
 */
public class BaseTokenValidationCallback<T> implements TokenValidationCallback<T> {

  private TokenStatus status = TokenStatus.INVALID;

  public void setTokenStatus(TokenStatus pStatus) {
    status = pStatus;
  }

  public TokenStatus getTokenStatus() {
    return status;
  }

  /* 
   * @see com.github.ibole.infrastructure.security.jwt.TokenValidationCallback#onInValid(java.lang.Object)
   */
  @Override
  public void onInValid(T jwtClaims) {
    setTokenStatus(TokenStatus.INVALID);
    
  }

  /* 
   * @see com.github.ibole.infrastructure.security.jwt.TokenValidationCallback#onValiated(java.lang.Object)
   */
  @Override
  public void onValiated(T jwtClaims) {
    setTokenStatus(TokenStatus.VALIDATED);
    
  }

  /* 
   * @see com.github.ibole.infrastructure.security.jwt.TokenValidationCallback#onExpired(java.lang.Object)
   */
  @Override
  public void onExpired(T jwtClaims) {
    setTokenStatus(TokenStatus.EXPIRED);
    
  }

  /* 
   * @see com.github.ibole.infrastructure.security.jwt.TokenValidationCallback#onError(java.lang.Object)
   */
  @Override
  public void onError(T jwtClaims) {
    setTokenStatus(TokenStatus.INVALID);
    
  }
}
