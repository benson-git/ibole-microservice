package com.github.ibole.microservice.common;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;

import com.github.ibole.infrastructure.common.dto.TransferObject;
import com.github.ibole.microservice.common.utils.EqualsUtil;
import com.github.ibole.microservice.common.utils.HashCodeUtil;
import com.github.ibole.microservice.common.utils.ToStringUtil;

import java.net.URI;
import java.util.List;

/**
 * Representation of host and port for target server. In cluster env, it will represent the list of
 * host and port for all the cluster nodes. In standalone env, only a pair of host and port is
 * represented.
 * 
 * @author bwang
 *
 */
public class ServerIdentifier implements TransferObject {

  private static final long serialVersionUID = 1L;

  public static final String BASE_KEY_PREFIX = "Root";

  private List<HostAndPort> hostAndPortList;
  // relative opaque URI
  private URI rootPath;

  private String defaultBaseKeyValue() {
    return '/' + BASE_KEY_PREFIX;
  }

  /**
   * Constructor definition.
   * 
   * @param rootPath the base key
   * @param hostAndPortList the list of host and port
   */
  public ServerIdentifier(String rootPath, List<HostAndPort> hostAndPortList) {
    this.rootPath =
        Strings.isNullOrEmpty(rootPath) ? URI.create(defaultBaseKeyValue()) : URI.create(rootPath);
    this.hostAndPortList = hostAndPortList;
  }

  /**
   * Constructor definition.
   * 
   * @param hostAndPortList the list of host and port
   */
  public ServerIdentifier(List<HostAndPort> hostAndPortList) {
    this.rootPath = URI.create(defaultBaseKeyValue());
    this.hostAndPortList = hostAndPortList;
  }

  /**
   * ServerIdentifier Constructor.
   * 
   * @param rootPath the base key
   * @param hostAndPortList the list of host and port
   */
  public ServerIdentifier(String rootPath, String hostAndPortList) {
    this.rootPath =
        Strings.isNullOrEmpty(rootPath) ? URI.create(defaultBaseKeyValue()) : URI.create(rootPath);
    setConnectionString(hostAndPortList);
  }


  /**
   * Get the root path.
   * 
   * @return the rootPath
   */
  public URI getRootPath() {
    return rootPath;
  }

  /**
   * Set the root path.
   * 
   * @param rootPath the rootPath to set
   */
  public void setRootPath(URI rootPath) {
    this.rootPath = rootPath;
  }

  /**
   * Get the list of host and port.
   * 
   * @return the hostAndPortList
   */
  public List<HostAndPort> getHostAndPortList() {
    return hostAndPortList;
  }

  /**
   * Set the list of HostAndPort.
   * 
   * @param hostAndPortList the hostAndPortList to set
   */
  public void setHostAndPortList(List<HostAndPort> hostAndPortList) {
    this.hostAndPortList = hostAndPortList;
  }

  /**
   * Get connection string of server.
   * 
   * @return the connection string of server.
   */
  public String getConnectionString() {

    StringBuilder conn = new StringBuilder();
    for (HostAndPort hostAndPort : hostAndPortList) {
      conn.append(hostAndPort).append(',');
    }
    conn = conn.deleteCharAt(conn.length() - 1);
    return conn.toString();
  }

  /**
   * Set server's connection string.
   * 
   * @param connectionString server's connection string
   */
  public void setConnectionString(String connectionString) {
    hostAndPortList = Lists.newArrayList();
    String[] tokens = connectionString.split(",");
    for (String token : tokens) {
      if (!Strings.isNullOrEmpty(token)) {
        hostAndPortList.add(HostAndPort.fromString(token));
      }
    }
  }

  /**
   * Indicates whether some other object is "equal to" this {@code ServerIdentifier}. The result is
   * {@code true} if and only if the argument is not {@code null} and is a {@code ServerIdentifier}
   * object that represents the same label (case-sensitively) as this {@code ServerIdentifier}.
   * 
   * @param obj the reference object with which to compare
   * @return {@code true} if this {@code ServerIdentifier} is the same as {@code obj}; {@code false}
   *         otherwise
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
    return EqualsUtil.equal(rootPath, other.rootPath)
        && EqualsUtil.equal(hostAndPortList, other.hostAndPortList);
  }

  /**
   * Returns a hash code for this {@code ServerIdentifier}.
   * 
   * <p>
   * This implementation is consistent with {@code equals}.
   * 
   * @return a hash code for this {@code ServerIdentifier}
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, rootPath);
    result = HashCodeUtil.hash(result, hostAndPortList);
    return result;
  }

  /**
   * Returns a string representation of this {@code ServerIdentifier}. This implementation returns a
   * representation based on the value and label.
   * 
   * @return a string representation of this {@code ServerIdentifier}
   */
  @SuppressWarnings("nls")
  @Override
  public String toString() {
    final StringBuilder sb = ToStringUtil.start("rootPath", rootPath);
    ToStringUtil.append(sb, "hostAndPortList", hostAndPortList);
    return ToStringUtil.end(sb);
  }

  /**
   * main.
   * 
   * @param args input
   */
  public static void main(String[] args) {

    URI test = URI.create("/test");

    test.getPath().equals("/test");

  }
}
