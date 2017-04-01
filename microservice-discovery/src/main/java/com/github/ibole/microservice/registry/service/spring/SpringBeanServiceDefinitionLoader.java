/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ibole.microservice.registry.service.spring;

import com.github.ibole.microservice.common.utils.ConcurrentSet;
import com.github.ibole.microservice.container.spring.SpringContainer;
import com.github.ibole.microservice.registry.service.ServiceDefinitionLoader;
import com.github.ibole.microservice.registry.service.ServiceExporter;
import com.github.ibole.microservice.registry.service.grpc.GrpcServiceDefinition;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.StandardMethodMetadata;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 * @author bwang
 *
 */
public class SpringBeanServiceDefinitionLoader extends ServiceDefinitionLoader<GrpcServiceDefinition> {

  private static final Logger LOG = LoggerFactory.getLogger(SpringBeanServiceDefinitionLoader.class.getName());
  private static final ConcurrentSet<GrpcServiceDefinition> services = new ConcurrentSet<GrpcServiceDefinition>();

  public SpringBeanServiceDefinitionLoader() {
    loadService();
  }

  private void loadService() {
    
    //find and get all ServiceExporter-enabled beans
    try {
      getBeanNamesByTypeWithAnnotation(ServiceExporter.class, BindableService.class)
              .forEach(name->{
                  BindableService srv = SpringContainer.getContext().getBeanFactory().getBean(name, BindableService.class);
                  ServerServiceDefinition serviceDefinition = srv.bindService();
                  //GRpcService gRpcServiceAnn = SpringContainer.getContext().findAnnotationOnBean(name, ServiceExporter.class);
                  //serviceDefinition  = bindInterceptors(serviceDefinition,gRpcServiceAnn,globalInterceptors);
                  //serverBuilder.addService(serviceDefinition);
                  //log.info("'{}' service has been registered.", srv.getClass().getName());
                  services.add(new GrpcServiceDefinition(serviceDefinition));
              });
    } catch (Exception e) { 
      LOG.warn("Exception happened when loading all service definitions", e);
    }
  }

  private <T> Stream<String> getBeanNamesByTypeWithAnnotation(Class<? extends Annotation> annotationType, Class<T> beanType) {

    return Stream.of(SpringContainer.getContext().getBeanNamesForType(beanType))
             .filter(name->{
                 BeanDefinition beanDefinition = SpringContainer.getContext().getBeanFactory().getBeanDefinition(name);
                 if( beanDefinition.getSource() instanceof StandardMethodMetadata) {
                     StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                     return metadata.isAnnotated(annotationType.getName());
                 }
                 return null!= SpringContainer.getContext().getBeanFactory().findAnnotationOnBean(name, annotationType);
             });
 }

  @Override
  public List<GrpcServiceDefinition> getServiceList() {
    return ImmutableList.copyOf(services);
  }

  @Override
  protected boolean isAvailable() {
    
    return true;
  }


  @Override
  protected int priority() {
   
    return 5;
  }

}
