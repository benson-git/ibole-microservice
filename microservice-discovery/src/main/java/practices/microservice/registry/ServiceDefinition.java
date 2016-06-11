/**
 * 
 */
package practices.microservice.registry;

import practices.microservice.common.EqualsUtil;
import practices.microservice.common.HashCodeUtil;
import practices.microservice.common.ToStringUtil;
import practices.microservice.common.to.TransferObject;

/**
 * @author bwang
 *
 */
public class ServiceDefinition implements TransferObject{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String serviceName;
	//TODO: decide the generic object
    private Object serviceObj;
    
    public ServiceDefinition(String serviceName, Object serviceObj){
    	this.serviceName = serviceName;
    	this.serviceObj = serviceObj;
    }
	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}
	/**
	 * @param servieName the servieName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	/**
	 * @return the serviceObj
	 */
	public Object getServiceObj() {
		return serviceObj;
	}
	/**
	 * @param serviceObj the serviceObj to set
	 */
	public void setServiceObj(Object serviceObj) {
		this.serviceObj = serviceObj;
	}
    
	/**
	 * Indicates whether some other object is "equal to" this
	 * {@code ServiceDefinition}. The result is {@code true} if and only if the
	 * argument is not {@code null} and is a {@code RegisterEntry} object that
	 * represents the same label (case-sensitively) as this {@code ServiceDefinition}.
	 * 
	 * @param obj
	 *            the reference object with which to compare
	 * @return {@code true} if this {@code ServiceDefinition} is the same as
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
		final ServiceDefinition other = (ServiceDefinition) obj;
		return EqualsUtil.equal(serviceName, other.serviceName)
				&& EqualsUtil.equal(serviceObj, other.serviceObj);
	}

	/**
	 * Returns a hash code for this {@code ServiceDefinition}.
	 * <p>
	 * This implementation is consistent with {@code equals}.
	 * 
	 * @return a hash code for this {@code ServiceDefinition}
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, serviceName);
		result = HashCodeUtil.hash(result, serviceObj);
		return result;
	}

	/**
	 * Returns a string representation of this {@code ServiceDefinition}. This
	 * implementation returns a representation based on the value and label.
	 * 
	 * @return a string representation of this {@code ServiceDefinition}
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		final StringBuilder sb = ToStringUtil.start("serviceName", serviceName);
		ToStringUtil.append(sb, "serviceObj", serviceObj);
		return ToStringUtil.end(sb);
	}
}
   
