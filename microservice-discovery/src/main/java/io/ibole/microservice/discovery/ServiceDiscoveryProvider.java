package io.ibole.microservice.discovery;

import io.ibole.infrastructure.common.exception.ProviderNotFoundException;

import java.util.ServiceLoader;


public abstract class ServiceDiscoveryProvider {

  private static final ServiceDiscoveryProvider provider =
      load(Thread.currentThread().getContextClassLoader());

  static final ServiceDiscoveryProvider load(ClassLoader cl) {
    ServiceLoader<ServiceDiscoveryProvider> providers =
        ServiceLoader.load(ServiceDiscoveryProvider.class, cl);
    ServiceDiscoveryProvider best = null;

    for (ServiceDiscoveryProvider current : providers) {
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
   */
  protected abstract boolean isAvailable();

  /**
   * A priority, from 0 to 10 that this provider should be used, taking the current environment into
   * consideration. 5 should be considered the default, and then tweaked based on environment
   * detection. A priority of 0 does not imply that the provider wouldn't work; just that it should
   * be last in line.
   */
  protected abstract int priority();

  /**
   * Returns the ClassLoader-wide default server.
   *
   * @throws ProviderNotFoundException if no provider is available
   */
  public static ServiceDiscoveryProvider provider() {
    if (provider == null) {
      throw new ProviderNotFoundException(
          "No functional server found. " + "Try adding a dependency on the rpc framework artifact");
    }
    return provider;
  }

  public abstract DiscoveryFactory<ServiceDiscovery<InstanceMetadata>> getDiscoveryFactory();
}
