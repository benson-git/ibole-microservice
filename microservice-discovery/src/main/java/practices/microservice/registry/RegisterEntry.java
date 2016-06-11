/**
 * 
 */
package practices.microservice.registry;

import java.util.Date;

import practices.microservice.common.EqualsUtil;
import practices.microservice.common.HashCodeUtil;
import practices.microservice.common.ToStringUtil;
import practices.microservice.common.to.TransferObject;


/**
 * @author bwang
 *
 */
public class RegisterEntry implements TransferObject {
	
	private static final long serialVersionUID = 1L;
	private int port;
	private String hostname;
	private String module;;
	private Date lastUpdated;
	private ServiceDefinition serviceDefinition;

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the module
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @param module
	 *            the module to set
	 */
	public void setModule(String module) {
		this.module = module;
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
	 * @return the serviceDefinition
	 */
	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	/**
	 * @param serviceDefinition
	 *            the serviceDefinition to set
	 */
	public void setServiceDefinition(ServiceDefinition serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
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
		return EqualsUtil.equal(port, other.port)
				&& EqualsUtil.equal(hostname, other.hostname)
				&& EqualsUtil.equal(module, other.module)
				&& EqualsUtil.equal(lastUpdated, other.lastUpdated)
				&& EqualsUtil.equal(serviceDefinition, other.serviceDefinition);
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
		result = HashCodeUtil.hash(result, port);
		result = HashCodeUtil.hash(result, hostname);
		result = HashCodeUtil.hash(result, module);
		result = HashCodeUtil.hash(result, lastUpdated);
		result = HashCodeUtil.hash(result, serviceDefinition);
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
		final StringBuilder sb = ToStringUtil.start("hostname", hostname);
		ToStringUtil.append(sb, "port", port);
		ToStringUtil.append(sb, "module", module);
		ToStringUtil.append(sb, "port", port);
		ToStringUtil.append(sb, "serviceDefinition", serviceDefinition);
		ToStringUtil.append(sb, "lastUpdated", lastUpdated);
		return ToStringUtil.end(sb);
	}
}
