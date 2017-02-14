package io.ibole.microservice.discovery;

import io.ibole.microservice.common.to.TransferObject;
import io.ibole.microservice.common.utils.EqualsUtil;
import io.ibole.microservice.common.utils.HashCodeUtil;
import io.ibole.microservice.common.utils.ToStringUtil;

import java.util.Date;


/**
 * RegistryEntry is the minimum unit for registry center.
 * 
 * <p>The hierarchical structure of registry center shows as below:
 *                         Root
 *                         -----
 *                           |
 *		---------------------------------------------------------------------
 *      |										 |							|
 *      Service Type(RPC)               		 Service Type (RPC)		    Service Type (MYSQL)
 *          |										|
 *      ---------------------                 -----------------------
 *      |                   |				  |						|
 *      Service Name     	Service Name   	  Service Name          Service Name
 *            |
 *      --------------------
 *      |                  |
 *      Service Contract   Service Contract
 *              |
 *      ---------------------------------     
 *      |             					| 
 *      Instance      					Instance
 *      (Value: Hostname:Port)          (Value: Hostname:Port) 
 *            
 *  A fully-qualified ZooKeeper name used to construct a gRPC channel will look as follows:
 *
 * 		zookeeper://host:port/serviceType/serviceName/serviceContract/instance
 *     example: zookeeper://host:port/rpc/Toprank/com.test.practices.GreeterSub/instance
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
  private String serviceName;
  private String serviceContract;
  private InstanceMetadata instanceMetadata;
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
   * @return the serviceContract
   */
  public String getServiceContract() {
    return serviceContract;
  }

  /**
   * @param serviceContract the serviceContract to set
   */
  public void setServiceContract(String serviceContract) {
    this.serviceContract = serviceContract;
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
   * @return the instanceMetadata
   */
  public InstanceMetadata getInstanceMetadata() {
    return instanceMetadata;
  }

  /**
   * @param instanceMetadata the instanceMetadata to set
   */
  public void setInstanceMetadata(InstanceMetadata instanceMetadatum) {
    this.instanceMetadata = instanceMetadatum;
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
        && EqualsUtil.equal(serviceContract, other.serviceContract)
        && EqualsUtil.equal(instanceMetadata, other.instanceMetadata)
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
    result = HashCodeUtil.hash(result, serviceContract);
    result = HashCodeUtil.hash(result, instanceMetadata);
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
    ToStringUtil.append(sb, "serviceContract", serviceContract);
    ToStringUtil.append(sb, "instanceMetadata", instanceMetadata);
    ToStringUtil.append(sb, "lastUpdated", lastUpdated);
    return ToStringUtil.end(sb);
  }
}
