package com.github.ibole.microservice.security.auth.jwt;

import com.google.common.collect.Lists;

import java.util.List;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/

/**
 * JWT claim object.
 * 
 * @author bwang
 *
 */
public class JwtObject {
  //jwt unique key
  private String jwtId;
  //who creates the token and signs it
  private String issuer;
  //to whom the token is intended to be sent.
  private String audience;
  //time to live for the token or time when the token will expire 
  private int ttlSeconds;
  //the subject/principal is whom the token is about
  private String subject;
  //the list of role id/code
  private List<String> roles = Lists.newArrayList();
  //对PC端使用由服务端主动发送的uuid到浏览器客户端存储起来备用，
  //对APP端使用手机串号，从而避免Token被盗用情况
  //Client identifier(Mobile: imei; PC: uuid)
  private String clientId;
  //carry user login id
  private String loginId;
  
  
  /**
   * @return the jwtId
   */
  public String getJwtId() {
    return jwtId;
  }
  /**
   * @param jwtId the jwtId to set
   */
  public void setJwtId(String jwtId) {
    this.jwtId = jwtId;
  }
  /**
   * @return the issuer
   */
  public String getIssuer() {
    return issuer;
  }
  /**  
   * @param issuer the issuer to set
   */
  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }
  /**
   * @return the audience
   */
  public String getAudience() {
    return audience;
  }
  /**
   * @param audience the audience to set
   */
  public void setAudience(String audience) {
    this.audience = audience;
  }

  /**
   * @return the ttlSeconds
   */
  public int getTtlSeconds() {
    return ttlSeconds;
  }
  /**
   * @param ttlSeconds the ttlSeconds to set
   */
  public void setTtlSeconds(int ttlSeconds) {
    this.ttlSeconds = ttlSeconds;
  }
  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }
  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }
  /**
   * @return the roles
   */
  public List<String> getRoles() {
    return roles;
  }
  /**
   * @param roles the roles to set
   */
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
  /**
   * @return the clientId
   */
  public String getClientId() {
    return clientId;
  }
  /**
   * @param clientId the clientId to set
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
  /**
   * @return the loginId
   */
  public String getLoginId() {
    return loginId;
  }
  /**
   * @param loginId the loginId to set
   */
  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }
  
  public static JwtObject getEmpty() {
    return new JwtObject();
  }
}
