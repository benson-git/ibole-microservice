/**
 * 
 */
package com.github.ibole.microservice.config.annotation;

import com.github.ibole.microservice.common.TLS;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * Annotation for service reference. 
 * Discover the expected service from registry center and return
 * instantiated client stub with the specific custom options
 * 
 * @author bwang
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Reference {

  Class<?> interfaceClass() default void.class;

  String interfaceName() default "";
  
  String preferredZone() default "";
  
  TLS usedTls() default TLS.UNKNOWN;

  String version() default "";
  
  int timeout() default 0;

}
