/**
 * 
 */
package practices.microservice.rpc.server;

import java.io.IOException;

/**
 * @author bwang
 *
 */
public interface RpcServer {
    /**
     * 
     * @param pPort
     * @param pUseTls
     */
	public void configure(int pPort, boolean pUseTls);
	/**
	 *  Bind and start the server.
	 */
	public void start() throws IOException;
	/**
	 * Forceful shutdown.
	 */
	public void stop() throws InterruptedException;
	/**
	 *  Waits for the server to become terminated.
	 *  Await termination on the main thread.
	 * 
	 */
	public void blockUntilShutdown() throws InterruptedException;
	/**
	 * Register service to service register center.
	 */
	public void registerService();
	
}
