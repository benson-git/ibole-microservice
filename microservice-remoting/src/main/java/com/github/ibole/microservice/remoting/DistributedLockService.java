
package com.github.ibole.microservice.remoting;


public interface DistributedLockService {

  boolean tryLock(String key);

  boolean tryReleaseLock(String key);

}
