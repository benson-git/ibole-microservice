package io.ibole.microservice.container.spring;

import io.ibole.microservice.container.IocContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Spring container.
 * 
 * @author bwang
 *
 */
public class SpringContainer implements IocContainer {

  private static final Logger logger = LoggerFactory.getLogger(SpringContainer.class);

  public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

  static ClassPathXmlApplicationContext context;

  public static ClassPathXmlApplicationContext getContext() {
    return context;
  }

  /**
   * Start container.
   */
  public void start() {
    context = new ClassPathXmlApplicationContext(DEFAULT_SPRING_CONFIG);
    context.start();
  }

  /**
   * Stop container.
   */
  public void stop() {
    try {
      if (context != null) {
        context.stop();
        context.close();
        context = null;
      }
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }
  }

}
