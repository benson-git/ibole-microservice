/**
 * 
 */
package practices.microservice.container.spring;

import practices.microservice.container.IocContainer;
import practices.microservice.container.IocContainerProvider;

/**
 * @author bwang
 *
 */
public class SpringIocContainerProvider extends IocContainerProvider {

	/* (non-Javadoc)
	 * @see practices.microservice.container.IocContainerProvider#isAvailable()
	 */
	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see practices.microservice.container.IocContainerProvider#priority()
	 */
	@Override
	protected int priority() {
		
		return 5;
	}

	/* (non-Javadoc)
	 * @see practices.microservice.container.IocContainerProvider#createIocContainer()
	 */
	@Override
	public IocContainer createIocContainer() {
		
		return new SpringContainer();
	}

}
