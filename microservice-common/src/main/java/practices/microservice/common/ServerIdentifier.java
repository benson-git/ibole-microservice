/**
 * 
 */
package practices.microservice.common;

import java.util.List;

import practices.microservice.common.to.TransferObject;
import practices.microservice.common.utils.EqualsUtil;
import practices.microservice.common.utils.HashCodeUtil;
import practices.microservice.common.utils.ToStringUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
	
	public final static String BASE_KEY = "Toprank";
	
	private static final long serialVersionUID = 1L;

	private ServiceType serviceType;

	private List<HostAndPort> hostAndPortList;

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
		@Override
		public String toString() {
			return value;
		}
	}
	/**
	 * @param serviceType
	 * @param baseKey
	 * @param hostAndPortList
	 */
	public ServerIdentifier(ServiceType serviceType, List<HostAndPort> hostAndPortList) {
		this.serviceType = serviceType;
		this.hostAndPortList = hostAndPortList;
	}

	/**
	 * @param serviceType
	 * @param baseKey
	 * @param hostAndPortList
	 */
	public ServerIdentifier(ServiceType serviceType, String hostAndPortList) {
		this.serviceType = serviceType;
		setConnectionString(hostAndPortList);
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
	public void setServiceType(ServiceType name) {
		this.serviceType = name;
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
	
	public void setConnectionString(String connectionString){
		hostAndPortList = Lists.newArrayList();
		String[] tokens = connectionString.split(",");
		for (String token : tokens){
			if(!Strings.isNullOrEmpty(token))
			{
				hostAndPortList.add(HostAndPort.fromString(token));
			}
		}
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
		return EqualsUtil.equal(serviceType, other.serviceType)
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
		result = HashCodeUtil.hash(result, serviceType.getValue());
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
		final StringBuilder sb = ToStringUtil.start("serviceType", serviceType.getValue());
		ToStringUtil.append(sb, "hostAndPortList", hostAndPortList);
		return ToStringUtil.end(sb);
	}
}
