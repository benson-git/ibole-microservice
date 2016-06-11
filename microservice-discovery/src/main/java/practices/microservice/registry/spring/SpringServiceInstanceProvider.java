/**
 * 
 */
package practices.microservice.registry.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import practices.microservice.registry.ServiceInstanceProvider;

/**
 * @author bwang
 *
 */
public class SpringServiceInstanceProvider extends ServiceInstanceProvider {
	
	private static ApplicationContext ctx;
	
	public SpringServiceInstanceProvider(){
	     ctx = new ClassPathXmlApplicationContext(
				"beans-annotation.xml");
	}

	/* (non-Javadoc)
	 * @see practices.rpc.service.registry.ServiceBeanProvider#isAvailable()
	 */
	@Override
	protected boolean isAvailable() {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see practices.rpc.service.registry.ServiceBeanProvider#priority()
	 */
	@Override
	protected int priority() {
		
		return 5;
	}

	/* (non-Javadoc)
	 * @see practices.rpc.service.registry.ServiceBeanProvider#getServiceBean(java.lang.Class)
	 */
	@Override
	public <T> T getServiceBean(Class<? extends T> type) {
		
		return ctx.getBean(type);
	}

}
