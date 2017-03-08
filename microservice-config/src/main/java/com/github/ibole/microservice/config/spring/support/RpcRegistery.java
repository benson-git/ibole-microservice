package com.github.ibole.microservice.config.spring.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * RpcRegistery.
 */
public class RpcRegistery implements InitializingBean, DisposableBean {

  private int timeout;

  private String token;

  private String address;
  /**
   * {@code ServerIdentifier.rootPath}
   */
  private String rootPath;

  @Override
  public void destroy() throws Exception {
    // TODO Auto-generated method stub
    // CommonRpcTcpServer.getInstance().stop();//停止
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub
    // if(port==0){
    // throw new Exception("parameter timeout port can not be null");
    // }
    // CommonRpcTcpServer.getInstance().setToken(token);
    // CommonRpcTcpServer.getInstance().setCodecType(codecType);
    // CommonRpcTcpServer.getInstance().setProcotolType(procotolType);
    // CommonRpcTcpServer.getInstance().start(port,timeout);
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * Get token.
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Set token.
   * @param token the token to set
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * Get address.
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Set address.
   * @param address the address to set
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Get root path.
   * @return the rootPath
   */
  public String getRootPath() {
    return rootPath;
  }

  /**
   * Set root path.
   * @param rootPath the type to set
   */
  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

}
