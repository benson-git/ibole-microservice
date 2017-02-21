package io.ibole.microservice.config.spring;

import io.ibole.microservice.config.spring.support.RpcReference;

import com.google.common.base.Strings;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 *Rpc Reference Parser.
 */
public class RpcReferenceParser implements BeanDefinitionParser {

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {

    String interfacename = element.getAttribute("interfacename");
    String id = element.getAttribute("id");
    int timeout = Strings.isNullOrEmpty(element.getAttribute("timeout")) ? 0
        : Integer.parseInt(element.getAttribute("timeout"));
    RootBeanDefinition beanDefinition = new RootBeanDefinition();
    beanDefinition.setBeanClass(RpcReference.class);
    beanDefinition.setLazyInit(false);

    beanDefinition.getPropertyValues().addPropertyValue("interfacename", interfacename);
    beanDefinition.getPropertyValues().addPropertyValue("beanName", id);
    beanDefinition.getPropertyValues().addPropertyValue("timeout", timeout);
    
    parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
    return beanDefinition;
  }

}
