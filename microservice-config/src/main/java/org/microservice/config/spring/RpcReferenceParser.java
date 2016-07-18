/**
 * 
 */
package org.microservice.config.spring;

import org.microservice.config.spring.support.RpcReference;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 *
 */
public class RpcReferenceParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		
		String interfacename = element.getAttribute("interfacename");
		String id = element.getAttribute("id");
		
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(RpcReference.class);
		beanDefinition.setLazyInit(false);
		
		beanDefinition.getPropertyValues().addPropertyValue("interfacename", interfacename);
        
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
		return beanDefinition;
	}

}
