package com.github.ibole.microservice.rpc.core;



import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * Sharing data between RPC and external application.
 * 
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class RpcContext {

  private static final ThreadLocal<HashMap<String, Object>> data = new ThreadLocal<HashMap<String, Object>>(){ 
    
    @Override
    protected HashMap<String, Object> initialValue() { 
        return new HashMap<>(); 
    }
  };
  
  private RpcContext() {
    throw new IllegalAccessError("Utility class");
  }

  
  public static Map<String, Object> getData(){
    return data.get();
  }
  
  public static void clear(){
    data.get().clear();
  }
}
