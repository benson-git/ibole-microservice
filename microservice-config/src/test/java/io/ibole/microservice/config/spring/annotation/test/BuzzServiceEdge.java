package io.ibole.microservice.config.spring.annotation.test;

import io.ibole.microservice.config.annotation.Reference;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


public class BuzzServiceEdge {

  @Reference(timeout = 3000)
  private DemoService demoService;
  
  public String doSayName(String name) {
      return demoService.sayName(name);
  }
}
