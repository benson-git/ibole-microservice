/**
 * 
 */
package practices.microservice.rpc.client.grpc;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.internal.SharedResourceHolder;
import io.grpc.internal.SharedResourceHolder.Resource;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * /** A Zookeeper-based {@link NameResolver}.
 * 
 * Example:
 * 
 * zk://zkserver0:1234 or
 * zk://zkserver0:1234/zkserver1:3456/zkserver2:3456
 *
 * @see ZkNameResolverFactory
 * 
 * @author bwang
 *
 */
public class ZkNameResolver extends NameResolver {

	private List<List<ResolvedServerInfo>> servers = new ArrayList<List<ResolvedServerInfo>>();
	private String authority;
	private String host;
	private int port;
	private URI nextTargetUri;
	private final Resource<ScheduledExecutorService> timerServiceResource;
	private final Resource<ExecutorService> executorResource;
	@GuardedBy("this")
	private boolean shutdown;
	@GuardedBy("this")
	private ScheduledExecutorService timerService;
	@GuardedBy("this")
	private ExecutorService executor;
	@GuardedBy("this")
	private ScheduledFuture<?> resolutionTask;
	@GuardedBy("this")
	private boolean resolving;
	@GuardedBy("this")
	private Listener listener;

	ZkNameResolver(URI targetUri, Attributes params, Resource<ScheduledExecutorService> timerService,
			Resource<ExecutorService> sharedChannelExecutor) {
		// Following just doing the check for the first authority.
		String targetPath = Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
		Preconditions.checkArgument(targetPath.startsWith("/"),
				"the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
		authority = Preconditions.checkNotNull(targetUri.getAuthority(), "nameUri (%s) doesn't have an authority",
				targetUri);
		host = Preconditions.checkNotNull(targetUri.getHost(), "host");
		if (targetUri.getPort() == -1) {
			Integer defaultPort = params.get(NameResolver.Factory.PARAMS_DEFAULT_PORT);
			if (defaultPort != null) {
				port = defaultPort;
			} else {
				throw new IllegalArgumentException("Authority '" + targetUri.getAuthority()
						+ "' doesn't contain a port, and default port is not set in params");
			}
		} else {
			port = targetUri.getPort();
		}
		// end of the checking for the first authority.
		servers.add(new ArrayList<ResolvedServerInfo>());
		nextTargetUri = nextUri(targetUri);
		this.timerServiceResource = timerService;
		this.executorResource = sharedChannelExecutor;
	}

	private List<List<ResolvedServerInfo>> parseURI(URI targetUri) throws UnknownHostException {

		String host = targetUri.getHost();
		int port = targetUri.getPort();
		if (port == -1) {
			return servers;
		}
		InetAddress inetAddr = InetAddress.getByName(host);
		servers.get(0).add(new ResolvedServerInfo(new InetSocketAddress(inetAddr, port), Attributes.EMPTY));

		servers = parseURI(nextUri(targetUri));

		return servers;
	}

	private URI nextUri(URI targetUri) {
		String targetPath = targetUri.getPath();
		String name = targetPath.substring(1);
		if(Strings.isNullOrEmpty(name)){
			return URI.create("//NULL");
		}
		URI nameUri = URI.create("//" + name);
		return nameUri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.grpc.NameResolver#getServiceAuthority()
	 */
	@Override
	public String getServiceAuthority() {

		return authority;
	}

	@Override
	public final synchronized void start(Listener listener) {
		Preconditions.checkState(this.listener == null, "already started");
		timerService = SharedResourceHolder.get(timerServiceResource);
		executor = SharedResourceHolder.get(executorResource);
		this.listener = Preconditions.checkNotNull(listener, "listener");
		resolve();
	}

	@Override
	public final synchronized void refresh() {
		Preconditions.checkState(listener != null, "not started");
		resolve();
	}

	private final Runnable resolutionRunnable = new Runnable() {
		@Override
		public void run() {
			Listener savedListener;
			synchronized (ZkNameResolver.this) {
				// If this task is started by refresh(), there might already be
				// a scheduled task.
				if (resolutionTask != null) {
					resolutionTask.cancel(false);
					resolutionTask = null;
				}
				if (shutdown) {
					return;
				}
				savedListener = listener;
				resolving = true;
			}
			try {
				try {
					InetAddress inetAddr = InetAddress.getByName(host);
					servers.get(0).add(new ResolvedServerInfo(new InetSocketAddress(inetAddr, port), Attributes.EMPTY));
					if(nextTargetUri != null){
						servers = parseURI(nextTargetUri);
					}
				} catch (UnknownHostException e) {
					synchronized (ZkNameResolver.this) {
						if (shutdown) {
							return;
						}
						// Because timerService is the single-threaded
						// GrpcUtil.TIMER_SERVICE in production,
						// we need to delegate the blocking work to the executor
						resolutionTask = timerService.schedule(resolutionRunnableOnExecutor, 1, TimeUnit.MINUTES);
					}
					savedListener.onError(Status.UNAVAILABLE.withCause(e));
					return;
				}
				savedListener.onUpdate(servers, Attributes.EMPTY);
			} finally {
				synchronized (ZkNameResolver.this) {
					resolving = false;
				}
			}
		}
	};

	private final Runnable resolutionRunnableOnExecutor = new Runnable() {
		@Override
		public void run() {
			synchronized (ZkNameResolver.this) {
				if (!shutdown) {
					executor.execute(resolutionRunnable);
				}
			}
		}
	};

	@GuardedBy("this")
	private void resolve() {
		if (resolving || shutdown) {
			return;
		}
		executor.execute(resolutionRunnable);
	}

	@Override
	public final synchronized void shutdown() {
		if (shutdown) {
			return;
		}
		shutdown = true;
		if (resolutionTask != null) {
			resolutionTask.cancel(false);
		}
		if (timerService != null) {
			timerService = SharedResourceHolder.release(timerServiceResource, timerService);
		}
		if (executor != null) {
			executor = SharedResourceHolder.release(executorResource, executor);
		}
	}

	final int getPort() {
		return port;
	}
}
