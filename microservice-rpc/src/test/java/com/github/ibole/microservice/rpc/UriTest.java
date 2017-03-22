package com.github.ibole.microservice.rpc;

import com.github.ibole.microservice.discovery.HostMetadata;

import com.google.common.base.Throwables;

import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;
import io.grpc.ResolvedServerInfoGroup.Builder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;

public class UriTest {

    static String zoneToPrefer = "test";
    static boolean usedTls = true;
	public static void main(String[] args) throws URISyntaxException, UnknownHostException {
		 URI targetUri = new URI("zk://UserService.user.service.ibole.github.com/?zone=test&tls=true");
		 System.out.println(targetUri.getHost());
		 System.out.println(targetUri.getAuthority());
		 System.out.println(targetUri.getQuery());
		 System.out.println(targetUri.getPath());
		 System.out.println(targetUri.toASCIIString());
		 
		 InetAddress[] allByName = InetAddress.getAllByName("127.0.0.1");
		 Arrays.stream(allByName).map( (ip) ->  {
		           String name = ip.getHostAddress();
		           System.out.println(name);
		           return name;
		         }
		     ).collect(Collectors.toList());
		     
		// String targetPath = targetUri.getPath();
		 //String name = targetPath.substring(1);
		 //System.out.println(name);
		 
		 //URI nameUri = URI.create("//" + name);
		 //System.out.println(nameUri.getAuthority());
		// System.out.println(nameUri.getHost() +":"+nameUri.getPort());
		 
		 List<HostMetadata> hosts = new ArrayList();
		 HostMetadata host1 = new HostMetadata("bwang", 4444, "test", true);
		 HostMetadata host2 = new HostMetadata("www.cnblogs.com", 80, "test1", true);
		 hosts.add(host1);
		 hosts.add(host2);
		 
		 Predicate<HostMetadata> predicateWithZoneAndTls = host -> zoneToPrefer.equalsIgnoreCase(host.getZone()) && usedTls == host.isUseTls();
		 Predicate<HostMetadata> predicateWithTls = host -> usedTls == host.isUseTls();
		 
		 List<ResolvedServerInfoGroup> preferredServers = filterResolvedServers(hosts, predicateWithZoneAndTls);
		 preferredServers.size();

	}

  private static List<ResolvedServerInfoGroup> filterResolvedServers(List<HostMetadata> newList, Predicate<HostMetadata> predicate) {
       return newList.stream().filter(predicate).map( hostandZone -> {
           InetAddress[] allByName;
           try {
                allByName = InetAddress.getAllByName(hostandZone.getHostname());
                Builder builder = ResolvedServerInfoGroup.builder();
                Stream.of(allByName).forEach( inetAddress -> {
                      builder.add(new ResolvedServerInfo(new InetSocketAddress(inetAddress, hostandZone.getPort())));
                });
                return builder.build();
               } catch (Exception e) {
                  throw Throwables.propagate(e);
               }
        }).collect(Collectors.toList());
  }
  
  
}
