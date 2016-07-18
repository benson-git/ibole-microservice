/**
 * 
 */
package org.microservice.config.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import practices.microservice.common.utils.ClassHelper;
import practices.microservice.rpc.client.RpcClientProvider;


/**
 * @param <T>
 *
 */
public class RpcReference<T> implements FactoryBean<T>, InitializingBean, DisposableBean {

	private String interfacename;

	private String token;

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcReference.class);
	

	@Override
	public void destroy() throws Exception {
         
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public T getObject() throws Exception {

		return RpcClientProvider.provider().getRpcClient().getRemotingService(getObjectType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getObjectType() {
		
		try {
			return (Class<T>) ClassHelper.forName(interfacename);
		} catch (ClassNotFoundException e) {
			
			LOGGER.error("Spring parse error", e);
		}
		return null;
	}

	@Override
	public boolean isSingleton() {
		
		return true;
	}

	/**
	 * @return the interfacename
	 */
	public String getInterfacename() {
		return interfacename;
	}

	/**
	 * @param interfacename
	 *            the interfacename to set
	 */
	public void setInterfacename(String interfacename) {
		this.interfacename = interfacename;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
}
