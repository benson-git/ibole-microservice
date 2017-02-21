package io.ibole.microservice.rpc.server;

/**
 * The interface of RpcServer.
 * 
 * @author bwang
 *
 */
public interface RpcServer {
  /**
   * Configure.
   * 
   * @param pPort the port
   * @param pUseTls the use TLS
   */
  public void configure(int pPort, boolean pUseTls);
  
  /**
   * Register Interceptor to add cross-cutting behavior to RPC server-side calls.
   * @param interceptor
   * @return 
   */
  public void registerInterceptor(RpcServerInterceptor interceptor);

  /**
   * Bind and start the server.
   */
  public void start();

  /**
   * Forceful shutdown.
   */
  public void stop() throws InterruptedException;

  /**
   * Graceful shutdown. Waits for the server to become terminated. Await termination on the main thread.
   * 
   */
  public void blockUntilShutdown() throws InterruptedException;
}
