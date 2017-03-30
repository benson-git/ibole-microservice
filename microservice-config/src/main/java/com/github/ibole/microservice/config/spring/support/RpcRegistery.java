package com.github.ibole.microservice.config.spring.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * RpcRegistery.
 */
public class RpcRegistery implements InitializingBean, DisposableBean {

  private int timeout;

  private String token;
  //config for single zk server: zkserver0:1234; 
  //config for cluster zk server:zkserver0:1234/zkserver1:3456/zkserver2:3456
  private String address;
  /**
   * {@code ServerIdentifier.rootPath}
   */
  private String rootPath;
  
  private String preferredZone;
  
  private boolean usedTls;

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
   * @return the preferredZone
   */
  public String getPreferredZone() {
    return preferredZone;
  }

  /**
   * @param preferredZone the preferredZone to set
   */
  public void setPreferredZone(String preferredZone) {
    this.preferredZone = preferredZone;
  }

  /**
   * @return the usedTls
   */
  public boolean isUsedTls() {
    return usedTls;
  }

  /**
   * @param usedTls the usedTls to set
   */
  public void setUsedTls(boolean usedTls) {
    this.usedTls = usedTls;
  }

  /**
   * Set root path.
   * @param rootPath the type to set
   */
  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

}
