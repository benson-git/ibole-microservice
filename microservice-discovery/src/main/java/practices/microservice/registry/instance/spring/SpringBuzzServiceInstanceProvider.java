/**
 * 
 */
package practices.microservice.registry.instance.spring;

import practices.microservice.registry.instance.BuzzServiceInstanceProvider;
import cc.toprank.byd.microservice.container.spring.SpringContainer;

/**
 * @author bwang
 *
 */
public class SpringBuzzServiceInstanceProvider extends BuzzServiceInstanceProvider {

	/* (non-Javadoc)
	 * @see practices.microservice.discovery.BuzzServiceInstanceProvider#isAvailable()
	 */
	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see practices.microservice.discovery.BuzzServiceInstanceProvider#priority()
	 */
	@Override
	protected int priority() {
		
		return 5;
	}

	/* (non-Javadoc)
	 * @see practices.microservice.discovery.BuzzServiceInstanceProvider#getServiceBean(java.lang.Class)
	 */
	@Override
	public <T> T getServiceBean(Class<? extends T> type) {
		
		return SpringContainer.getContext().getBean(type);
	}

}
