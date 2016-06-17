/**
 * 
 */
package practices.microservice.registry;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import practices.microservice.common.to.TransferObject;
import practices.microservice.common.utils.EqualsUtil;
import practices.microservice.common.utils.HashCodeUtil;
import practices.microservice.common.utils.ToStringUtil;


/**
 * RegistryEntry is the minimum unit for registry center.
 * 
 * The hierarchical structure of registry center shows as below:
 * 
 *  				   Root Node---Application---
 *            							|
 *		---------------------------------------------------------------------
 *      |										 |							|
 *      Service Type(RPC)               		 Service Type (RPC)		    Service Type (MYSQL)
 *          |										|
 *      ---------------------                 -----------------------
 *      |                   |				  |						|
 *      Service Details     Service Details   Service Details       Service Details
 *            |                   
 *      ---------------------------------     
 *      |             					| 
 *      Instance      					Instance
 *      (Value: Hostname:Port)          (Value: Hostname:Port) 
 *            
 *  A fully-qualified ZooKeeper name used to construct a gRPC channel will look as follows:
 *
 * 		zookeeper://host:port/application/serviceType/serviceDetails/instance
 * 
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
	
	public enum ServiceType{
		RPC("rpc"),
		DB("mysql");
		
		private String value;
		
		ServiceType(String type){
			value = type;
		}
		
		public String getValue(){
			return value;
		}
	}
	
	private static final long serialVersionUID = 1L;
	private String application;
	private ServiceType serviceType;
	private String serviceName;
	private List<InstanceDetails> instanceDetails = Collections.emptyList();
	//Service description, like simple API docs
	private String description;
	private Date lastUpdated;

	/**
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(String application) {
		this.application = application;
	}

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
	 * @return the serviceType
	 */
	public ServiceType getServiceType() {
		return serviceType;
	}

	/**
	 * @param serviceType the serviceType to set
	 */
	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
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
	 * @return the instanceDetails
	 */
	public List<InstanceDetails> getInstanceDetails() {
		return instanceDetails;
	}

	/**
	 * @param instanceDetails the instanceDetails to set
	 */
	public void setInstanceDetails(List<InstanceDetails> instanceDetails) {
		this.instanceDetails = instanceDetails;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated
	 *            the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}


	/**
	 * Indicates whether some other object is "equal to" this
	 * {@code RegisterEntry}. The result is {@code true} if and only if the
	 * argument is not {@code null} and is a {@code RegisterEntry} object that
	 * represents the same label (case-sensitively) as this {@code RegisterEntry}.
	 * 
	 * @param obj
	 *            the reference object with which to compare
	 * @return {@code true} if this {@code RegisterEntry} is the same as
	 *         {@code obj}; {@code false} otherwise
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
		return EqualsUtil.equal(application, other.application)
				&& EqualsUtil.equal(serviceType, other.serviceType)
				&& EqualsUtil.equal(serviceName, other.serviceName)
				&& EqualsUtil.equal(instanceDetails, other.instanceDetails)
				&& EqualsUtil.equal(lastUpdated, other.lastUpdated);

	}

	/**
	 * Returns a hash code for this {@code RegisterEntry}.
	 * <p>
	 * This implementation is consistent with {@code equals}.
	 * 
	 * @return a hash code for this {@code RegisterEntry}
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, application);
		result = HashCodeUtil.hash(result, serviceName);
		result = HashCodeUtil.hash(result, serviceType);;
		result = HashCodeUtil.hash(result, instanceDetails);
		result = HashCodeUtil.hash(result, lastUpdated);
		return result;
	}

	/**
	 * Returns a string representation of this {@code RegisterEntry}. This
	 * implementation returns a representation based on the value and label.
	 * 
	 * @return a string representation of this {@code RegisterEntry}
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		final StringBuilder sb = ToStringUtil.start("application", application);
		ToStringUtil.append(sb, "serviceName", serviceName);
		ToStringUtil.append(sb, "serviceType", serviceType);
		ToStringUtil.append(sb, "instanceDetails", instanceDetails);
		ToStringUtil.append(sb, "lastUpdated", lastUpdated);
		return ToStringUtil.end(sb);
	}
}
