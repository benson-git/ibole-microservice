package com.github.ibole.microservice.registry.service.spring;

import com.github.ibole.microservice.container.spring.SpringContainer;
import com.github.ibole.microservice.registry.service.BuzzServiceInstanceProvider;
import com.github.ibole.microservice.registry.service.ServiceImplementationException;

/**
 * @author bwang
 *
 */
public class SpringBuzzServiceInstanceProvider extends BuzzServiceInstanceProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.ibole.microservice.discovery.BuzzServiceInstanceProvider#isAvailable()
   */
  @Override
  protected boolean isAvailable() {

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.ibole.microservice.discovery.BuzzServiceInstanceProvider#priority()
   */
  @Override
  protected int priority() {

    return 5;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.github.ibole.microservice.discovery.BuzzServiceInstanceProvider#getServiceBean(java.lang.
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
