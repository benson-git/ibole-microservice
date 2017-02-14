package io.ibole.microservice.registry.instance.spring;

import io.ibole.microservice.container.spring.SpringContainer;
import io.ibole.microservice.registry.instance.BuzzServiceInstanceProvider;
import io.ibole.microservice.registry.instance.ServiceImplementationException;

/**
 * @author bwang
 *
 */
public class SpringBuzzServiceInstanceProvider extends BuzzServiceInstanceProvider {

  /*
   * (non-Javadoc)
   * 
   * @see cc.toprank.byd.microservice.discovery.BuzzServiceInstanceProvider#isAvailable()
   */
  @Override
  protected boolean isAvailable() {

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cc.toprank.byd.microservice.discovery.BuzzServiceInstanceProvider#priority()
   */
  @Override
  protected int priority() {

    return 5;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cc.toprank.byd.microservice.discovery.BuzzServiceInstanceProvider#getServiceBean(java.lang.
   * Class)
   */
  @Override
  public <T> T getServiceBean(Class<? extends T> type) throws ServiceImplementationException {

    try {
      return SpringContainer.getContext().getBean(type);
    } catch (Exception e) {
      throw new ServiceImplementationException("Get service implementation bean error.", e);
    }
  }

}
