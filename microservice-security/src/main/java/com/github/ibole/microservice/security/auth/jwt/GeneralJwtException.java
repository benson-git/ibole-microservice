/**
 * 
 */
package com.github.ibole.microservice.security.auth.jwt;

/*********************************************************************************************.
 * 
 * 
 * <p>版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public class GeneralJwtException extends RuntimeException {

  public GeneralJwtException(String msg, Throwable ex) {
    super(msg, ex);
  }

  public GeneralJwtException(Throwable ex) {
    super(ex);
  }
}
