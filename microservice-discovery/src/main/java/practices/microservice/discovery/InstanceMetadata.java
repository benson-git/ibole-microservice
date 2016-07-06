package practices.microservice.discovery;

import java.util.UUID;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import practices.microservice.common.to.TransferObject;
import practices.microservice.common.utils.EqualsUtil;
import practices.microservice.common.utils.HashCodeUtil;
import practices.microservice.common.utils.ToStringUtil;

public class InstanceMetadata implements TransferObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("id")
	private UUID id = UUID.randomUUID();
    @JsonProperty("port")
	private int port;
    @JsonProperty("hostname")
	private String hostname;
    @JsonProperty("useTls")
	private boolean useTls;
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param id
	 * @param port
	 * @param hostname
	 * @param useTls
	 */
	@JsonCreator
	public InstanceMetadata(@JsonProperty("id") UUID id, @JsonProperty("hostname") String hostname, @JsonProperty("port") int port,  @JsonProperty("useTls") boolean useTls) {
		this.id = id;
		this.port = port;
		this.hostname = hostname;
		this.useTls = useTls;
	}
	
	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
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
	 * {@code InstanceMetadata}. The result is {@code true} if and only if the
	 * argument is not {@code null} and is a {@code InstanceMetadata} object that
	 * represents the same label (case-sensitively) as this {@code InstanceMetadata}.
	 * 
	 * @param obj
	 *            the reference object with which to compare
	 * @return {@code true} if this {@code InstanceMetadata} is the same as
	 *         {@code obj}; {@code false} otherwise
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InstanceMetadata)) {
			return false;
		}
		final InstanceMetadata other = (InstanceMetadata) obj;
		return EqualsUtil.equal(id, other.id)
				&& EqualsUtil.equal(hostname, other.hostname)
				&& EqualsUtil.equal(port, other.port)
				&& EqualsUtil.equal(useTls, other.useTls);

	}

	/**
	 * Returns a hash code for this {@code InstanceMetadata}.
	 * <p>
	 * This implementation is consistent with {@code equals}.
	 * 
	 * @return a hash code for this {@code InstanceMetadata}
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, id);
		result = HashCodeUtil.hash(result, hostname);
		result = HashCodeUtil.hash(result, port);
		result = HashCodeUtil.hash(result, useTls);;
		return result;
	}

	/**
	 * Returns a string representation of this {@code InstanceMetadata}. This
	 * implementation returns a representation based on the value and label.
	 * 
	 * @return a string representation of this {@code InstanceMetadata}
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		final StringBuilder sb = ToStringUtil.start("id", id);
		ToStringUtil.append(sb, "hostname", hostname);
		ToStringUtil.append(sb, "port", port);
		ToStringUtil.append(sb, "useTls", useTls);
		return ToStringUtil.end(sb);
	}
}
