package io.ibole.microservice.config.spring.annotation.test;

import io.ibole.microservice.config.annotation.Reference;

import org.springframework.stereotype.Service;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/

@Service("buzzServiceEdge")
public class BuzzServiceEdge {

  @Reference(timeout = 3000)
  private DemoService demoService;
  
  public String doSayName(String name) {
      return demoService.sayName(name);
  }
}
