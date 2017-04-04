/**
 * 
 */
package com.github.ibole.microservice.config.spring.support;

import com.github.ibole.microservice.config.annotation.Reference;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
 *
 */
public class RpcAnnotation implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor, ApplicationContextAware{

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcReference.class.getName());

  private final String COMMA_SPLIT_PATTERN = ",";

  private String annotationPackage;

  private String[] annotationPackages;
  
  private ApplicationContext applicationContext;
  
  public void setAnnotationPackage(String annotationPackage) {
    this.annotationPackage = annotationPackage;
    if (!StringUtils.isEmpty(this.annotationPackage))
      this.annotationPackages = this.annotationPackage.split(this.COMMA_SPLIT_PATTERN);
  }
  
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (!isMatchPackage(bean)) {
      return bean;
    }
    Method[] methods = bean.getClass().getMethods();
    for (Method method : methods) {
      String name = method.getName();
      if (name.length() > 3 && name.startsWith("set") && method.getParameterTypes().length == 1
          && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
        try {
          Reference reference = method.getAnnotation(Reference.class);
          if (reference != null) {
            Object value = inferReference(reference, method.getParameterTypes()[0]);
            if (value != null) {
              method.invoke(bean, new Object[] {value});
            }
          }
        } catch (Throwable e) {
          LOGGER.error("Failed to init remote service reference at method " + name + " in class "
              + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
        }
      }
    }
    Field[] fields = bean.getClass().getDeclaredFields();
    for (Field field : fields) {
      try {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        Reference reference = field.getAnnotation(Reference.class);
        if (reference != null) {
          Object value = inferReference(reference, field.getType());
          if (value != null) {
            field.set(bean, value);
          }
        }
      } catch (Throwable e) {
        LOGGER.error("Failed to init remote service reference at filed '" + field.getName()
            + "' in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
      }
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }
  /**
  * Scan{@link com.github.ibole.microservice.config.annotation.Reference} Annotation.
  */ 
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    if (StringUtils.isEmpty(annotationPackage)) {
      return;
    }
    if (beanFactory instanceof BeanDefinitionRegistry) {
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
      ClassPathBeanDefinitionScanner scanner =
          new ClassPathBeanDefinitionScanner(beanDefinitionRegistry, true);
      AnnotationTypeFilter filter = new AnnotationTypeFilter(Reference.class);
      scanner.addIncludeFilter(filter);
      scanner.scan(annotationPackages);
    }

  }

  @Override
  public void destroy() throws Exception {
    //do nothing.
    
  }
  
  /**
   * @return the applicationContext
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @SuppressWarnings({"rawtypes"})
  private Object inferReference(Reference reference, Class<?> referenceClazz) {
    String interfaceName;
    if (!"".equals(reference.interfaceName())) {
      interfaceName = reference.interfaceName();
    } else if (!void.class.equals(reference.interfaceClass())) {
      interfaceName = reference.interfaceClass().getName();
    } else if (referenceClazz.isInterface()) {
      interfaceName = referenceClazz.getName();
    } else {
      //here we support to get the bean by the concrete class type 
      interfaceName = referenceClazz.getName();
    }
    RpcReference<?> rpcReference = new RpcReference(interfaceName, reference.preferredZone(), reference.usedTls(),
            reference.timeout());

    try {
      return rpcReference.getObject();
    } catch (Exception e) {
      throw new BeanInitializationException("Get object error happened.", e);
    }    
  }
  
  private boolean isMatchPackage(Object bean) {
    if (annotationPackages == null || annotationPackages.length == 0) {
        return true;
    }
    String beanClassName = bean.getClass().getName();
    for (String pkg : annotationPackages) {
        if (beanClassName.startsWith(pkg)) {
            return true;
        }
    }
    return false;
}
}
