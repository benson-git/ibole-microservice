/////ibole.microservice.config.spring;
/////ibole.microservice.config.spring.support.RpcAnnotation;
/////*    */ import org.springframework.beans.MutablePropertyValues;
/////*    */ import org.springframework.beans.factory.config.BeanDefinition;
/////*    */ import org.springframework.beans.factory.support.BeanDefinitionRegistry;
/////*    */ import org.springframework.beans.factory.support.RootBeanDefinition;
/////*    */ import org.springframework.beans.factory.xml.BeanDefinitionParser;
/////*    */ import org.springframework.beans.factory.xml.ParserContext;
/////*    */ import org.w3c.dom.Element;
///*    */ 
/////*    */ public class RpcAnnotationParser
/////*    */   implements BeanDefinitionParser
/////*    */ {
/////*    */   public BeanDefinition parse(Element element, ParserContext parserContext)
/////*    */   {
/////* 29 */     String id = element.getAttribute("id");
/////* 30 */     String annotationPackage = element.getAttribute("package");
/////*    */ 
/////* 32 */     RootBeanDefinition beanDefinition = new RootBeanDefinition();
/////* 33 */     beanDefinition.setBeanClass(RpcAnnotation.class);
/////* 34 */     beanDefinition.setLazyInit(false);
/////*    */ 
/////* 36 */     beanDefinition.getPropertyValues().addPropertyValue("annotationPackage", annotationPackage);
/////*    */ 
/////* 38 */     parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
/////* 39 */     return beanDefinition;
/////*    */   }
/////*    */ }
