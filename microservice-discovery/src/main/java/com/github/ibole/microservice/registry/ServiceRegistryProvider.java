package com.github.ibole.microservice.registry;

import com.github.ibole.infrastructure.common.exception.ProviderNotFoundException;
import com.github.ibole.microservice.discovery.InstanceMetadata;

import java.util.ServiceLoader;

/**
 * 
 * @author bwang
 *
 */
public abstract class ServiceRegistryProvider {

  private static final ServiceRegistryProvider provider =
      load(Thread.currentThread().getContextClassLoader());

  static final ServiceRegistryProvider load(ClassLoader cl) {
    ServiceLoader<ServiceRegistryProvider> providers =
        ServiceLoader.load(ServiceRegistryProvider.class, cl);
    ServiceRegistryProvider best = null;

    for (ServiceRegistryProvider current : providers) {
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
   * @return true if the registry provider is available, otherwise return false
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
   * @return the service registry provider ServiceRegistryProvider
   * @throws ProviderNotFoundException if no provider is available
   */
  public static ServiceRegistryProvider provider() {
    if (provider == null) {
      throw new ProviderNotFoundException(
          "No functional server found. " + "Try adding a dependency on the rpc framework artifact");
    }
    return provider;
  }

  public abstract RegistryFactory<ServiceRegistry<InstanceMetadata>> getRegistryFactory();
}
