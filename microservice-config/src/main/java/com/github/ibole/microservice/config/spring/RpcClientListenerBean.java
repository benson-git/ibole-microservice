package com.github.ibole.microservice.config.spring;

import com.github.ibole.microservice.common.ServerIdentifier;
import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.config.rpc.client.RpcClientProvider;
import com.github.ibole.microservice.config.spring.support.RpcRegistery;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;


public class RpcClientListenerBean
    implements ApplicationContextAware, ApplicationListener<ApplicationEvent>, BeanFactoryPostProcessor {

  private ApplicationContext applicationContext;
  
  private String serverHostOverride;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;

  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    //Prevent repeated execution
    if (applicationContext.getParent() == null) {
      onStartedEvent(event);
      onStoppedEvent(event);
      onClosedEvent(event);
    }
  }

  private void onStartedEvent(ApplicationEvent event) {
    if (ContextStartedEvent.class.getName().equals(event.getClass().getName())) {
      startRpcClient();
    }
  }

  /**
   * 
   */
  private void startRpcClient() {
    // Map<String, RpcRegistery> providerConfigMap =
    // BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RpcRegistery.class,
    // false, false);
    RpcRegistery rpcRegistery =
        BeanFactoryUtils.beanOfType(applicationContext, RpcRegistery.class);
    ServerIdentifier identifier =
        new ServerIdentifier(rpcRegistery.getRootPath(), rpcRegistery.getAddress());
    ClientOptions clientOptions = ClientOptions.DEFAULT;
    clientOptions = clientOptions.withRegistryCenterAddress(identifier)
            .withZoneToPrefer(rpcRegistery.getPreferredZone())
            .withServerHostOverride(getServerHostOverride())
            .withUsedTls(rpcRegistery.isUsedTls());
    RpcClientProvider.provider().getRpcClient().initialize(clientOptions);
    RpcClientProvider.provider().getRpcClient().start();
  }

  private void onStoppedEvent(ApplicationEvent event) {
    if (ContextStoppedEvent.class.getName().equals(event.getClass().getName())) {
      RpcClientProvider.provider().getRpcClient().stop();
    }
  }

  private void onClosedEvent(ApplicationEvent event) {

    if (ContextClosedEvent.class.getName().equals(event.getClass().getName())) {
      RpcClientProvider.provider().getRpcClient().stop();
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
   
    startRpcClient();
  }

  /**
   * @return the serverHostOverride
   */
  public String getServerHostOverride() {
    return serverHostOverride;
  }

  /**
   * @param serverHostOverride the serverHostOverride to set
   */
  public void setServerHostOverride(String serverHostOverride) {
    this.serverHostOverride = serverHostOverride;
  }


}
