package io.ibole.microservice.config.spring;

import io.ibole.microservice.config.spring.support.RpcService;

import com.google.common.base.Strings;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Rpc Service Parser.
 */
public class RpcServiceParser implements BeanDefinitionParser {

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {

    String id = element.getAttribute("id");
    String interfaceName = element.getAttribute("interface");
    String className = element.getAttribute("class");
    String ref = element.getAttribute("ref");
    
    if (Strings.isNullOrEmpty(id)) {
      String generatedBeanName = element.getAttribute("name");
      if (Strings.isNullOrEmpty(generatedBeanName)) {
        generatedBeanName = element.getAttribute("interface");
      }
      id = generatedBeanName;
    }
    if (!Strings.isNullOrEmpty(id)) {
      if (parserContext.getRegistry().containsBeanDefinition(id)) {
        throw new IllegalStateException("Duplicate spring bean id " + id);
      }
    }
    
    RootBeanDefinition beanDefinition = new RootBeanDefinition();
    beanDefinition.setBeanClass(RpcService.class);
    beanDefinition.setLazyInit(false);
    
    beanDefinition.getPropertyValues().addPropertyValue("id", id);
    beanDefinition.getPropertyValues().addPropertyValue("interfaceName", interfaceName);
    beanDefinition.getPropertyValues().addPropertyValue("implementationClass", className);
    beanDefinition.getPropertyValues().addPropertyValue("ref", ref);

    parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
    return beanDefinition;
  }

}
