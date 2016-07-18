package org.microservice.config.spring;

import org.microservice.config.spring.support.RpcService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
/**
 *
 */
public class RpcServiceParser implements BeanDefinitionParser {
	
	public RpcServiceParser(){
		
	}
	
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		
		String id = element.getAttribute("id");
		String interfacename = element.getAttribute("interfacename");
		String ref=element.getAttribute("ref");
		
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(RpcService.class);
		beanDefinition.setLazyInit(false);
		beanDefinition.getPropertyValues().addPropertyValue("id", id);
        beanDefinition.getPropertyValues().addPropertyValue("interfacename", interfacename);
        beanDefinition.getPropertyValues().addPropertyValue("ref", ref);
        
        parserContext.getRegistry().registerBeanDefinition(interfacename, beanDefinition);
		return beanDefinition;
	}

}
