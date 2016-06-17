package practices.microservice.registry;

import practices.microservice.common.to.TransferObject;
import practices.microservice.common.utils.EqualsUtil;
import practices.microservice.common.utils.HashCodeUtil;
import practices.microservice.common.utils.ToStringUtil;

public class InstanceDetails implements TransferObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int port;
	private String hostname;
	private boolean useTls;
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
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
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	/**
	 * @return the useTls
	 */
	public boolean isUseTls() {
		return useTls;
	}
	/**
	 * @param useTls the useTls to set
	 */
	public void setUseTls(boolean useTls) {
		this.useTls = useTls;
	}
	

	/**
	 * Indicates whether some other object is "equal to" this
	 * {@code InstanceDetails}. The result is {@code true} if and only if the
	 * argument is not {@code null} and is a {@code InstanceDetails} object that
	 * represents the same label (case-sensitively) as this {@code InstanceDetails}.
	 * 
	 * @param obj
	 *            the reference object with which to compare
	 * @return {@code true} if this {@code InstanceDetails} is the same as
	 *         {@code obj}; {@code false} otherwise
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InstanceDetails)) {
			return false;
		}
		final InstanceDetails other = (InstanceDetails) obj;
		return EqualsUtil.equal(hostname, other.hostname)
				&& EqualsUtil.equal(port, other.port)
				&& EqualsUtil.equal(useTls, other.useTls);

	}

	/**
	 * Returns a hash code for this {@code InstanceDetails}.
	 * <p>
	 * This implementation is consistent with {@code equals}.
	 * 
	 * @return a hash code for this {@code InstanceDetails}
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, hostname);
		result = HashCodeUtil.hash(result, port);
		result = HashCodeUtil.hash(result, useTls);;
		return result;
	}

	/**
	 * Returns a string representation of this {@code InstanceDetails}. This
	 * implementation returns a representation based on the value and label.
	 * 
	 * @return a string representation of this {@code InstanceDetails}
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		final StringBuilder sb = ToStringUtil.start("hostname", hostname);
		ToStringUtil.append(sb, "port", port);
		ToStringUtil.append(sb, "useTls", useTls);
		return ToStringUtil.end(sb);
	}
}
