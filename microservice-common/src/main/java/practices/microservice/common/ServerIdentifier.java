/**
 * 
 */
package practices.microservice.common;

import java.util.List;

import practices.microservice.common.to.TransferObject;
import practices.microservice.common.utils.EqualsUtil;
import practices.microservice.common.utils.HashCodeUtil;
import practices.microservice.common.utils.ToStringUtil;

import com.google.common.net.HostAndPort;

/**
 *  Representation of host and port for target server.
 *  In cluster env, it will represent the list of host and port for all the cluster nodes.
 *  In standalone env, only a pair of host and port is represented.
 * 
 * @author bwang
 *
 */
public class ServerIdentifier  implements TransferObject{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String identifier;
	
	private List<HostAndPort> hostAndPortList;

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String name) {
		this.identifier = name;
	}


	/**
	 * @return the hostAndPortList
	 */
	public List<HostAndPort> getHostAndPortList() {
		return hostAndPortList;
	}

	/**
	 * @param hostAndPortList the hostAndPortList to set
	 */
	public void setHostAndPortList(List<HostAndPort> hostAndPortList) {
		this.hostAndPortList = hostAndPortList;
	}
	
	public String getConnectionString(){
		
		StringBuilder conn = new StringBuilder();
		for(HostAndPort hostAndPort : hostAndPortList){
			conn.append(hostAndPort).append(',');
		}
		conn = conn.deleteCharAt(conn.length()-1);
		return conn.toString();
	}
    
	/**
	 * Indicates whether some other object is "equal to" this
	 * {@code ServerIdentifier}. The result is {@code true} if and only if the
	 * argument is not {@code null} and is a {@code ServerIdentifier} object that
	 * represents the same label (case-sensitively) as this {@code ServerIdentifier}.
	 * 
	 * @param obj
	 *            the reference object with which to compare
	 * @return {@code true} if this {@code ServerIdentifier} is the same as
	 *         {@code obj}; {@code false} otherwise
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ServerIdentifier)) {
			return false;
		}
		final ServerIdentifier other = (ServerIdentifier) obj;
		return EqualsUtil.equal(identifier, other.identifier)
				&& EqualsUtil.equal(hostAndPortList, other.hostAndPortList);
	}

	/**
	 * Returns a hash code for this {@code ServerIdentifier}.
	 * <p>
	 * This implementation is consistent with {@code equals}.
	 * 
	 * @return a hash code for this {@code ServerIdentifier}
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, identifier);
		result = HashCodeUtil.hash(result, hostAndPortList);
		return result;
	}

	/**
	 * Returns a string representation of this {@code ServerIdentifier}. This
	 * implementation returns a representation based on the value and label.
	 * 
	 * @return a string representation of this {@code ServerIdentifier}
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		final StringBuilder sb = ToStringUtil.start("identifier", identifier);
		ToStringUtil.append(sb, "HostAndPortList", hostAndPortList);
		return ToStringUtil.end(sb);
	}
}
