package com.github.ibole.microservice.container.spring;


import com.github.ibole.microservice.container.IocContainer;
import com.github.ibole.microservice.container.IocContainerProvider;

/**
 * @author bwang
 *
 */
public class SpringIocContainerProvider extends IocContainerProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.github.ibole.microservice.container.IocContainerProvider#isAvailable()
   */
  @Override
  protected boolean isAvailable() {

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.ibole.microservice.container.IocContainerProvider#priority()
   */
  @Override
  protected int priority() {

    return 5;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.ibole.microservice.container.IocContainerProvider#createIocContainer()
   */
  @Override
  public IocContainer createIocContainer() {

    return new SpringContainer();
  }

}
