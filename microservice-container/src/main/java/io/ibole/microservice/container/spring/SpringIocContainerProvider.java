package io.ibole.microservice.container.spring;


import io.ibole.microservice.container.IocContainer;
import io.ibole.microservice.container.IocContainerProvider;

/**
 * @author bwang
 *
 */
public class SpringIocContainerProvider extends IocContainerProvider {

  /*
   * (non-Javadoc)
   * 
   * @see cc.toprank.byd.microservice.container.IocContainerProvider#isAvailable()
   */
  @Override
  protected boolean isAvailable() {

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cc.toprank.byd.microservice.container.IocContainerProvider#priority()
   */
  @Override
  protected int priority() {

    return 5;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cc.toprank.byd.microservice.container.IocContainerProvider#createIocContainer()
   */
  @Override
  public IocContainer createIocContainer() {

    return new SpringContainer();
  }

}
