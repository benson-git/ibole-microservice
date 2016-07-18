package org.microservice.config.spring.support;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
/**
 * 
 *  
 */
public class RpcRegistery implements InitializingBean, DisposableBean {
	
	private int timeout;
	
	private String token;
	
	private String address;
	/**
	 *  {@code ServerIdentifier.ServiceType}
	 */
	private String type;
	
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		//CommonRpcTcpServer.getInstance().stop();//停止
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		//if(port==0){
			//throw new Exception("parameter  timeout port can not be null");
		//}
		//CommonRpcTcpServer.getInstance().setToken(token);
		//CommonRpcTcpServer.getInstance().setCodecType(codecType);
		//CommonRpcTcpServer.getInstance().setProcotolType(procotolType);
		//CommonRpcTcpServer.getInstance().start(port,timeout);
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
