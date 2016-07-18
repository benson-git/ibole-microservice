/**
 * 
 */
package org.microservice.config.spring.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 *
 */
public class RpcService implements DisposableBean, ApplicationContextAware, ApplicationListener<ApplicationEvent>{
	
	private String interfacename;
	
	private String ref;//服务类bean value
	
	private ApplicationContext applicationContext;
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
//		// TODO Auto-generated method stub
//		if(StringUtils.isNullOrEmpty(filterRef)||!(applicationContext.getBean(filterRef) instanceof RpcFilter)){//为空
//			CommonRpcTcpServer.getInstance().registerProcessor(interfacename, applicationContext.getBean(ref),null);
//		}else{
//			CommonRpcTcpServer.getInstance().registerProcessor(interfacename, applicationContext.getBean(ref),(RpcFilter)applicationContext.getBean(filterRef));
//		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		
		this.applicationContext=applicationContext;
	}

	/**
	 * @param interfacename the interfacename to set
	 */
	public void setInterfacename(String interfacename) {
		this.interfacename = interfacename;
	}

	/**
	 * @param ref the ref to set
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}

	/**
	 * @return the interfacename
	 */
	public String getInterfacename() {
		return interfacename;
	}

	/**
	 * @return the ref
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * @return the applicationContext
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
