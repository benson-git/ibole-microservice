package org.microservice.config.spring;

import org.microservice.config.spring.support.RpcRegistery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import practices.microservice.common.ServerIdentifier;
import practices.microservice.common.ServerIdentifier.ServiceType;
import practices.microservice.rpc.client.RpcClientProvider;

public class RpcClientListenerBean implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientListenerBean.class);

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		onStartedEvent(event);
		onStoppedEvent(event);
		onClosedEvent(event);
	}
	
	private void onStartedEvent(ApplicationEvent event){
		if (ContextStartedEvent.class.getName().equals(event.getClass().getName())) {
		    //Map<String, RpcRegistery> providerConfigMap =  BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RpcRegistery.class, false, false);
			RpcRegistery rpcRegistery = BeanFactoryUtils.beanOfType(applicationContext, RpcRegistery.class);
			ServerIdentifier identifier = new ServerIdentifier(ServiceType.valueOf(rpcRegistery.getType()), rpcRegistery.getAddress());
			RpcClientProvider.provider().getRpcClient().initialize(identifier);
			RpcClientProvider.provider().getRpcClient().start();
	        if (LOGGER.isInfoEnabled()) {
	        	LOGGER.info("The RPC client ready on spring started.");
	        }
		}
	}
	
	private void onStoppedEvent(ApplicationEvent event){
		if (ContextStoppedEvent.class.getName().equals(event.getClass().getName())) {
			RpcClientProvider.provider().getRpcClient().stop();
	        if (LOGGER.isInfoEnabled()) {
	        	LOGGER.info("The RPC client ready on spring stopped.");
	        }
		}
	}
	
	private void onClosedEvent(ApplicationEvent event){
		
		if (ContextClosedEvent.class.getName().equals(event.getClass().getName())) {
			RpcClientProvider.provider().getRpcClient().stop();
	        if (LOGGER.isInfoEnabled()) {
	        	LOGGER.info("The RPC client ready on spring closed.");
	        }
		}
	}


}
