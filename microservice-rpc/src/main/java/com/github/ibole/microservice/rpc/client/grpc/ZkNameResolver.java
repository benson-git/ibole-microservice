package com.github.ibole.microservice.rpc.client.grpc;

import com.github.ibole.microservice.config.rpc.client.ClientOptions;
import com.github.ibole.microservice.discovery.HostMetadata;
import com.github.ibole.microservice.discovery.ServiceDiscovery;
import com.github.ibole.microservice.discovery.ServiceDiscoveryProvider;
import com.github.ibole.microservice.rpc.client.exception.RpcClientException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Zookeeper-based {@link NameResolver}.
 * 
 * <pre>
 * FORMAT WILL BE: zk://serviceContract
 *                      ---------------
 *                            |
 *                      Service Name 
 * (e.g. zk://routeguide.RouteGuide (grpc service name: 'service package' + 'service name' boths are defined in proto file))     
 * </pre>
 * @see ZkNameResolverProvider
 * 
 * @author bwang
 *
 */
public class ZkNameResolver extends NameResolver {
  
  private static Logger LOGGER = LoggerFactory.getLogger(ZkNameResolver.class.getName());
  
  private ServiceDiscovery<HostMetadata> discovery = null;
  private final URI targetUri;
  private final Attributes params;
  private final ClientOptions callOptions;
  
  /**
   * ZkNameResolver Constructor.
   * @param targetUri the target service Uri
   * @param params the additional parameters
   * @param zookeeperAddress 
   * @param zoneToPrefer the preferred host zone
   * @param usedTls if the tls is enable
   */
  public ZkNameResolver(URI targetUri, Attributes params, ClientOptions callOptions) {
    // Following just doing the check for the first authority.
    Preconditions.checkNotNull(targetUri.getAuthority(), "authority");
    this.targetUri = targetUri;
    this.params = params;
    this.callOptions = callOptions;
    this.discovery =
        ServiceDiscoveryProvider.provider().getDiscoveryFactory()
            .getServiceDiscovery(callOptions.getRegistryCenterAddress());
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.grpc.NameResolver#getServiceAuthority()
   */
  @Override
  public String getServiceAuthority() {

    return targetUri.getAuthority();
  }
  
  @Override
  public final synchronized void start(Listener listener) {
    discovery.start();
    String serviceName = targetUri.getAuthority();
    List<HostMetadata> hostList = discovery.getInstanceList(serviceName);
    if (hostList == null || hostList.isEmpty()) {
      LOGGER.error("No services are registered for '{}' in registry center '{}'!", serviceName,
          discovery.getIdentifier());
      throw new RpcClientException("No services found!");
    }
   
    List<EquivalentAddressGroup> resolvedServers;
    // Find the service servers with the same preference zone.
    resolvedServers = filterResolvedServers(hostList, predicateZone(callOptions));
    // Find the service servers without preference zone filtering if no preference service server found.
    if (resolvedServers.isEmpty()) {
      resolvedServers = filterResolvedServers(hostList, predicateTls(callOptions));
    }

    listener.onAddresses(resolvedServers, params);
    //watch service node changes and fire the even
    discovery.watchForCacheUpdates(
        serviceName,
        hostMetadateList -> {
          //filtering with zone condition
          List<EquivalentAddressGroup> updatedServers =
              filterResolvedServers(hostMetadateList, predicateZone(callOptions));
          if (updatedServers.isEmpty()) {
            //filtering without zone condition
            updatedServers = filterResolvedServers(hostMetadateList, predicateTls(callOptions));
          }

          if (!updatedServers.isEmpty()) {
            listener.onAddresses(updatedServers, params);
              LOGGER.info("Watch updates for service '{}', "
                  + "latest server list {} after the updating.", serviceName,
                  updatedServers.toString());
          } else {
            LOGGER.warn("Watch updates - no servers are found for service '{}'.", serviceName);

          }
        });

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("ZkNameResolver is start.");
      LOGGER.info("ZkNameResolver resolved servers '{}'", resolvedServers);
    }
  }
  
  /**
   * Re-resolve the name.
   *
   * <p>Can only be called after {@link #start} has been called.
   *
   * <p>This is only a hint. Implementation takes it as a signal but may not start resolution
   * immediately. It should never throw.
   *
   * <p>The default implementation is no-op.
   */
  @Override
  public void refresh() {}
  
  @Override
  public final synchronized void shutdown() {
    try {
      discovery.destroy();
    } catch (Exception ex) {
      LOGGER.error("ZkNameResolver shutdown error happened", ex);
      throw new RpcClientException(ex);
    }
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("ZkNameResolver is shutdown.");
    }
  }
  
  private List<EquivalentAddressGroup> filterResolvedServers(List<HostMetadata> newList, Predicate<HostMetadata> predicate) {
    return newList.stream().filter(predicate).map( hostandZone -> {
        InetAddress[] allByName;
        try {
             //DNS:One hostname can map to multi-ip address
             allByName = InetAddress.getAllByName(hostandZone.getHostname());
             List<SocketAddress> addrs = Stream.of(allByName).map( inetAddress -> 
                 new InetSocketAddress(inetAddress, hostandZone.getPort())).collect(Collectors.toList());
             return new EquivalentAddressGroup(addrs, params);
            } catch (Exception e) {
               throw Throwables.propagate(e);
            }
     }).collect(Collectors.toList());
  }
  
  private Predicate<HostMetadata> predicateZone(ClientOptions callOptions){
    Predicate<HostMetadata> predicateWithZoneAndTls = host -> {
      if(callOptions.isUsedTls() != host.isUseTls()){
        return false;
      }  
      if(Strings.isNullOrEmpty(callOptions.getZoneToPrefer())){
        return true;
      } 
      return callOptions.getZoneToPrefer().equalsIgnoreCase(Strings.nullToEmpty(host.getZone()));   
     };
    return predicateWithZoneAndTls;
  }
  
  private Predicate<HostMetadata> predicateTls(ClientOptions callOptions){
    Predicate<HostMetadata> predicateWithTls = host -> callOptions.isUsedTls() == host.isUseTls();
    return predicateWithTls;
  }
  
}
