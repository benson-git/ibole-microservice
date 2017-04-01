package com.github.ibole.microservice.discovery;

import com.github.ibole.infrastructure.common.dto.TransferObject;
import com.github.ibole.microservice.common.utils.EqualsUtil;
import com.github.ibole.microservice.common.utils.HashCodeUtil;
import com.github.ibole.microservice.common.utils.ToStringUtil;

import java.util.Date;


/**
 * RegistryEntry is the minimum unit for registry center.
 * 
 *
 *  The hierarchical structure of registry center shows as below:
 *  <pre>
 *                         Root
 *                         -----
 *                           |
 *      --------------------------------------------------------------------
 *      |										 |                          |
 *      Service Type(RPC)                 Service Type (RPC)		Service Type (MYSQL)
 *          |									   |
 *      ---------------------                 -----------------------
 *      |                   |				  |						|
 *      Service Name     	Service Name   	  Service Name          Service Name
 *            |
 *      ---------------------------------     
 *      |             					| 
 *      HostMetadata      			    HostMetadata
 *      (Value: Hostname:Port:Tls)      (Value: Hostname:Port:Tls) 
 *  
 * </pre>
 *  A fully-qualified ZooKeeper name used to construct a gRPC channel will look as follows:
 *  <pre>
 * 		zookeeper://host:port/serviceType/serviceName/serviceContract/instance
 *     example: zookeeper://host:port/rpc/ibole/com.test.practices.GreeterSub/hostname:port:true
 *  </pre>
 *  Here zookeeper is the scheme identifying the name-system.
 *	host:port identifies an authoritative name-server for this scheme (i.e., a Zookeeper server). 
 *	The host can be an IP address or a DNS name. Finally /path/service/instance is the Zookeeper name to be resolved.
 *  
 *  Each service is a zookeeper node, and each instance is a child node of the corresponding service. 
 *  For example, a MySQL service may have multiple instances, /mysql/1, /mysql/2, /mysql/3. 
 *  The name of the service or instance, as well as an optional path is specified by the service provider.
 *  The data in service nodes is empty. Each instance node stores its address in the format of host:port, 
 *  where host can be either hostname or IP address.
 *  
 * @author bwang
 *
 */
public class RegisterEntry implements TransferObject {

  private static final long serialVersionUID = 1L;
  //the specified service for discovery
  private String serviceName;
  private HostMetadata hostMetadata;
  // Service description, like simple API docs
  private String description;
  private Date lastUpdated;

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * @param serviceName the serviceName to set
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }


  /**
   * @return the hostMetadata
   */
  public HostMetadata getHostMetadata() {
    return hostMetadata;
  }

  /**
   * @param instanceMetadatum the hostMetadata to set
   */
  public void setHostMetadata(HostMetadata instanceMetadatum) {
    this.hostMetadata = instanceMetadatum;
  }

  /**
   * @return the lastUpdated
   */
  public Date getLastUpdated() {
    return lastUpdated;
  }

  /**
   * @param lastUpdated the lastUpdated to set
   */
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }


  /**
   * Indicates whether some other object is "equal to" this {@code RegisterEntry}. The result is
   * {@code true} if and only if the argument is not {@code null} and is a {@code RegisterEntry}
   * object that represents the same label (case-sensitively) as this {@code RegisterEntry}.
   * 
   * @param obj the reference object with which to compare
   * @return {@code true} if this {@code RegisterEntry} is the same as {@code obj}; {@code false}
   *         otherwise
   * @see #hashCode()
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RegisterEntry)) {
      return false;
    }
    final RegisterEntry other = (RegisterEntry) obj;
    return EqualsUtil.equal(serviceName, other.serviceName)
        && EqualsUtil.equal(hostMetadata, other.hostMetadata)
        && EqualsUtil.equal(lastUpdated, other.lastUpdated);

  }

  /**
   * Returns a hash code for this {@code RegisterEntry}.
   * 
   * <p>This implementation is consistent with {@code equals}.
   * 
   * @return a hash code for this {@code RegisterEntry}
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, serviceName);
    result = HashCodeUtil.hash(result, hostMetadata);
    result = HashCodeUtil.hash(result, lastUpdated);
    return result;
  }

  /**
   * Returns a string representation of this {@code RegisterEntry}. This implementation returns a
   * representation based on the value and label.
   * 
   * @return a string representation of this {@code RegisterEntry}
   */
  @SuppressWarnings("nls")
  @Override
  public String toString() {
    final StringBuilder sb = ToStringUtil.start("serviceName", serviceName);
    ToStringUtil.append(sb, "hostMetadata", hostMetadata);
    ToStringUtil.append(sb, "lastUpdated", lastUpdated);
    return ToStringUtil.end(sb);
  }
}
