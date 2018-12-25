package com.github.ibole.microservice.registry.service;

import com.github.ibole.microservice.common.exception.ProviderNotFoundException;

import java.util.ServiceLoader;


/**
 * Lookup the business service instance from somewhere.
 * 
 * @author bwang
 *
 */
public abstract class BuzzServiceInstanceProvider {
  private static final BuzzServiceInstanceProvider provider =
      load(Thread.currentThread().getContextClassLoader());

  static final BuzzServiceInstanceProvider load(ClassLoader cl) {
    ServiceLoader<BuzzServiceInstanceProvider> providers =
        ServiceLoader.load(BuzzServiceInstanceProvider.class, cl);
    BuzzServiceInstanceProvider best = null;

    for (BuzzServiceInstanceProvider current : providers) {
      if (!current.isAvailable()) {
        continue;
      } else if (best == null) {
        best = current;
      } else if (current.priority() > best.priority()) {
        best = current;
      }
    }
    return best;
  }

  /**
   * Whether this provider is available for use, taking the current environment into consideration.
   * If {@code false}, no other methods are safe to be called.
   * @return the business service instance provider BuzzServiceInstanceProvider
   */
  protected abstract boolean isAvailable();

  /**
   * A priority, from 0 to 10 that this provider should be used, taking the current environment into
   * consideration. 5 should be considered the default, and then tweaked based on environment
   * detection. A priority of 0 does not imply that the provider wouldn't work; just that it should
   * be last in line.
   * @return the priority int
   */
  protected abstract int priority();

  /**
   * Returns the ClassLoader-wide default server.
   *
   * @return provider BuzzServiceInstanceProvider
   * @throws ProviderNotFoundException if no provider is available
   * 
   */
  public static BuzzServiceInstanceProvider provider() {
    if (provider == null) {
      throw new ProviderNotFoundException(
          "No functional server found. " + "Try adding a dependency on the rpc framework artifact");
    }
    return provider;
  }

  public abstract <T> T getServiceBean(final Class<? extends T> type)
      throws ServiceImplementationException;

}
