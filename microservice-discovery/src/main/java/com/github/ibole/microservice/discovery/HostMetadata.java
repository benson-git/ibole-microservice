package com.github.ibole.microservice.discovery;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;

import com.github.ibole.infrastructure.common.dto.TransferObject;
import com.github.ibole.microservice.common.utils.EqualsUtil;
import com.github.ibole.microservice.common.utils.HashCodeUtil;
import com.github.ibole.microservice.common.utils.ToStringUtil;

/**
 * Host Metadata.
 * 
 * The aim of "zone": In order to reduce network latency, 
 * service consumer prefers to consume the target service provider in the same zone,
 * avoid to do remoting call cross zone.
 *  
 * @author bwang
 *
 */
public class HostMetadata implements TransferObject {

  private static final long serialVersionUID = 1L;
  
  @JsonProperty("port")
  private int port;
  
  @JsonProperty("hostname")
  private String hostname;
  
  @JsonProperty("zone")
  private String zone;
  
  @JsonProperty("useTls")
  private boolean useTls;

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port int
   * @param hostname String
   * @param zone the zone of the server
   * @param useTls boolean
   */
  @JsonCreator
  public HostMetadata(@JsonProperty("hostname") String hostname,
      @JsonProperty("port") int port, @JsonProperty("zone") String zone, @JsonProperty("useTls") boolean useTls) {
    this.port = port;
    this.hostname = hostname;
    this.zone = zone;
    this.useTls = useTls;
  }
  
  /**
   * @param port int
   * @param hostname String
   * @param useTls boolean
   */
  @JsonIgnore
  public HostMetadata(@JsonProperty("hostname") String hostname,
      @JsonProperty("port") int port, @JsonProperty("useTls") boolean useTls) {
      this(hostname, port, "*", useTls);
  }

  /**
   * build instance meta Id.
   * @return the unique id
   */
  public String generateKey() {
    StringBuilder builder = new StringBuilder();
    return builder.append(hostname).append(":").append(port).toString();
  }

  @JsonIgnore
  public URI getHostEndpoint() {
    return URI.create("//"+hostname+":"+port+"/?tls="+useTls+"&zone="+zone);
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
   * @return the zone
   */
  public String getZone() {
    return zone;
  }

  /**
   * @param zone the zone to set
   */
  public void setZone(String zone) {
    this.zone = zone;
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
   * Indicates whether some other object is "equal to" this {@code HostMetadata}. The result is
   * {@code true} if and only if the argument is not {@code null} and is a {@code HostMetadata}
   * object that represents the same label (case-sensitively) as this {@code HostMetadata}.
   * 
   * @param obj the reference object with which to compare
   * @return {@code true} if this {@code HostMetadata} is the same as {@code obj}; {@code false}
   *         otherwise
   * @see #hashCode()
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HostMetadata)) {
      return false;
    }
    final HostMetadata other = (HostMetadata) obj;
    return EqualsUtil.equal(hostname, other.hostname)
        && EqualsUtil.equal(port, other.port)
        && EqualsUtil.equal(zone, other.zone)
        && EqualsUtil.equal(useTls, other.useTls);

  }

  /**
   * Returns a hash code for this {@code HostMetadata}.
   * 
   * <p>This implementation is consistent with {@code equals}.
   * 
   * @return a hash code for this {@code HostMetadata}
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, hostname);
    result = HashCodeUtil.hash(result, port);
    result = HashCodeUtil.hash(result, zone);
    result = HashCodeUtil.hash(result, useTls);;
    return result;
  }

  /**
   * Returns a string representation of this {@code HostMetadata}. This implementation returns a
   * representation based on the value and label.
   * 
   * @return a string representation of this {@code HostMetadata}
   */
  @SuppressWarnings("nls")
  @Override
  public String toString() {
    final StringBuilder sb = ToStringUtil.start("hostname", hostname);
    ToStringUtil.append(sb, "port", port);
    ToStringUtil.append(sb, "zone", zone);
    ToStringUtil.append(sb, "useTls", useTls);
    return ToStringUtil.end(sb);
  }
}
