package com.github.ibole.microservice.common;

import java.io.Serializable;

/**
 * Created by bwang on 2016/9/4.
 */
public class MiniDeviceInfoTo implements Serializable {
    /**
   * 
   */
  private static final long serialVersionUID = 1L;
    //手机型号
    private String model;
    //OS版本信息
    private String osVersion;
    //当前包的版本号
    private String versionCode;
    //本机串号imei
    private String imei;
    //IP
    private String ipAddress;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getImei() {
        return imei == null? "" : imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
      return ipAddress;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIpAddress(String ipAddress) {
      this.ipAddress = ipAddress;
    }
    
}
