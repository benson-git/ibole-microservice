package com.github.ibole.microservice.container;



import com.github.ibole.infrastructure.common.exception.ProviderNotFoundException;

import java.util.ServiceLoader;

/**
 * Provider of IOC Container.
 * @author bwang
 *
 */
public abstract class IocContainerProvider {
	private static final IocContainerProvider provider = load(Thread
			.currentThread().getContextClassLoader());

	static final IocContainerProvider load(ClassLoader cl) {
		ServiceLoader<IocContainerProvider> providers = ServiceLoader.load(
				IocContainerProvider.class, cl);
		IocContainerProvider best = null;

		for (IocContainerProvider current : providers) {
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
	 * Whether this provider is available for use, taking the current
	 * environment into consideration. If {@code false}, no other methods are
	 * safe to be called.
	 */
	protected abstract boolean isAvailable();

	/**
	 * A priority, from 0 to 10 that this provider should be used, taking the
	 * current environment into consideration. 5 should be considered the
	 * default, and then tweaked based on environment detection. A priority of 0
	 * does not imply that the provider wouldn't work; just that it should be
	 * last in line.
	 */
	protected abstract int priority();

	/**
	 * Returns the ClassLoader-wide default server.
	 *
	 * @throws ProviderNotFoundException
	 *             if no provider is available
	 */
	public static IocContainerProvider provider() {
		if (provider == null) {
			throw new ProviderNotFoundException("No functional server found. "
					+ "Try adding a dependency on the rpc framework artifact");
		}
		return provider;
	}

	public abstract IocContainer createIocContainer();
}
