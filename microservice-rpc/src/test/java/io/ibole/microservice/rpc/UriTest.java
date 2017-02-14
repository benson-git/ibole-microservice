package io.ibole.microservice.rpc;

import java.net.URI;
import java.net.URISyntaxException;

public class UriTest {

	public static void main(String[] args) throws URISyntaxException {
		 URI targetUri = new URI("dns://8.8.8.8/service/instance");
		 System.out.println(targetUri.getAuthority());
		 System.out.println(targetUri.getPath());
		 String targetPath = targetUri.getPath();
		 String name = targetPath.substring(1);
		 System.out.println(name);
		 
		 URI nameUri = URI.create("//" + name);
		 System.out.println(nameUri.getAuthority());
		 System.out.println(nameUri.getHost() +":"+nameUri.getPort());
	}

}
