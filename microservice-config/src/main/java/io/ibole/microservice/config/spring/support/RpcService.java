package io.ibole.microservice.config.spring.support;

import io.ibole.microservice.common.utils.ClassHelper;

import com.google.common.base.Strings;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * RPC service.
 */
public class RpcService
    implements DisposableBean, ApplicationContextAware, ApplicationListener<ApplicationEvent> {

  // 接口类型
  private String interfaceName;

  private String implementationClass;

  private String ref;// 服务类bean value

  private ApplicationContext applicationContext;

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    // // TODO Auto-generated method stub
    // if(StringUtils.isNullOrEmpty(filterRef)||!(applicationContext.getBean(filterRef) instanceof
    // RpcFilter)){//为空
    // CommonRpcTcpServer.getInstance().registerProcessor(interfacename,
    // applicationContext.getBean(ref),null);
    // }else{
    // CommonRpcTcpServer.getInstance().registerProcessor(interfacename,
    // applicationContext.getBean(ref),(RpcFilter)applicationContext.getBean(filterRef));
    // }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    this.applicationContext = applicationContext;
  }

  /**
   * @param interfaceName the interfaceName to set.
   */
  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  // public void setInterface(Class<?> interfaceClass) {
  // if (interfaceClass != null && !interfaceClass.isInterface()) {
  // throw new IllegalStateException(
  // "The interface class " + interfaceClass + " is not a interface!");
  // }
  // this.interfaceClass = interfaceClass;
  // setInterface(interfaceClass == null ? (String) null : interfaceClass.getName());
  // }

  /**
   * @param ref the ref to set.
   */
  public void setRef(String ref) {
    this.ref = ref;
  }

  /**
   * Get interface name of the service.
   * 
   * @return the interfaceName
   */
  public String getInterfaceName() {
    return interfaceName;
  }

  public String getImplementationClass() {
    return implementationClass;
  }

  public void setImplementationClass(String implementationClass) {
    this.implementationClass = implementationClass;
  }

  /**
   * Get reference service id/name.
   * 
   * @return the reference bean name
   */
  public String getRef() {
    return ref;
  }

  /**
   * get Interface Class.
   */
  public Class<?> getImplementClass() {

    try {
      if (ref != null) {
        return ClassHelper.forName(ref);
      }
      if (!Strings.isNullOrEmpty(implementationClass)) {
        return ClassHelper.forName(interfaceName);
      }
    } catch (ClassNotFoundException t) {
      throw new IllegalStateException(t.getMessage(), t);
    }
    return null;
  }

  /**
   * Get Application Context.
   * @return the applicationContext
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void destroy() throws Exception {
    //nothing to do

  }
}
