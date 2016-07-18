/**
 * 
 */
package org.microservice.config.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 
 * @author bwang
 *
 */
public class RpcNamespaceHandler extends NamespaceHandlerSupport {


	@Override
	public void init() {
		
		registerBeanDefinitionParser("reference", new RpcReferenceParser());
		registerBeanDefinitionParser("service", new RpcServiceParser());
		registerBeanDefinitionParser("registry", new RpcRegisteryParser());
	}

}
