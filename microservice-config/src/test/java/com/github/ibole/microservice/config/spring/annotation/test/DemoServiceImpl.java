/**
 * 
 */
package com.github.ibole.microservice.config.spring.annotation.test;

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
public class DemoServiceImpl implements DemoService {

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.config.spring.annotation.test.DemoService#sayName(java.lang.String)
   */
  @Override
  public String sayName(String name) {
    
    return "Hello "+name;
  }

}
