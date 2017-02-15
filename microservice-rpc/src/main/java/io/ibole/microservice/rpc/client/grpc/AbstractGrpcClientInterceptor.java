package io.ibole.microservice.rpc.client.grpc;



import io.grpc.ClientInterceptor;
import io.grpc.Metadata;

import java.util.List;
import java.util.Map;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * 
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public abstract class AbstractGrpcClientInterceptor implements ClientInterceptor{

  
  public Metadata toHeaders(Map<String, List<String>> metadata) {
    Metadata headers = new Metadata();
    if (metadata != null) {
      for (String key : metadata.keySet()) {
        Metadata.Key<String> headerKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
        for (String value : metadata.get(key)) {
          headers.put(headerKey, value);
        }
      }
    }
    return headers;
  }

  public Metadata toHeaders(String key, String value) {
    Metadata headers = new Metadata();
    if (key != null && value != null) {
      Metadata.Key<String> headerKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
      headers.put(headerKey, value);
    }
    return headers;
  }
}
