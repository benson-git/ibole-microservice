package com.github.ibole.microservice.security.auth.jwt.jose4j;

import com.github.ibole.microservice.security.auth.jwt.JwtProvider;
import com.github.ibole.microservice.security.auth.jwt.TokenAuthenticator;

import org.jose4j.jwa.AlgorithmFactoryFactory;

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
public class Jose4jJwtProvider extends JwtProvider {

  /* (non-Javadoc)
   * @see org.toprank.infrastructure.security.jwt.JwtProvider#isAvailable()
   */
  @Override
  protected boolean isAvailable() {
   
    return true;
  }

  /* (non-Javadoc)
   * @see org.toprank.infrastructure.security.jwt.JwtProvider#priority()
   */
  @Override
  protected int priority() {
   
    return 5;
  }

  /* (non-Javadoc)
   * @see org.toprank.infrastructure.security.jwt.JwtProvider#createTokenGenerator()
   */
  @Override
  public TokenAuthenticator createTokenGenerator() {
    // Initialized jose4j
    AlgorithmFactoryFactory.getInstance();
    return new EcJose4jTokenAuthenticator();
  }

}
