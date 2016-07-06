package practices.microservice.rpc.client.grpc;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.internal.GrpcUtil;

import java.net.URI;

public class ZkNameResolverFactory extends NameResolver.Factory {

	public static final String SCHEME = "zk";
	private static final ZkNameResolverFactory instance = new ZkNameResolverFactory();

	@Override
	public NameResolver newNameResolver(URI targetUri, Attributes params) {
		if (SCHEME.equals(targetUri.getScheme())) {
			return new ZkNameResolver(targetUri, params, GrpcUtil.TIMER_SERVICE,
					GrpcUtil.SHARED_CHANNEL_EXECUTOR);
		} else {
			return null;
		}
	}

	@Override
	public String getDefaultScheme() {

		return SCHEME;
	}

	private ZkNameResolverFactory() {
	}

	public static ZkNameResolverFactory getInstance() {
		return instance;
	}
}
