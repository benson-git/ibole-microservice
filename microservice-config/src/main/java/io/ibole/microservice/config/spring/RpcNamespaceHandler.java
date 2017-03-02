package io.ibole.microservice.config.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Rpc Namespace Handler.
 * @author bwang
 *
 */
public class RpcNamespaceHandler extends NamespaceHandlerSupport {


  @Override
  public void init() {

    registerBeanDefinitionParser("reference", new RpcReferenceParser());
    registerBeanDefinitionParser("service", new RpcServiceParser());
    registerBeanDefinitionParser("registry", new RpcRegisteryParser());
    registerBeanDefinitionParser("annotation", new RpcAnnotationParser());
  }

}
