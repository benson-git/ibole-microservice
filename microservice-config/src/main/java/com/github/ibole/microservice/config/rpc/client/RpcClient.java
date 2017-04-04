package com.github.ibole.microservice.config.rpc.client;


/**
 * The interface of RPC client.
 * 
 * @author bwang
 *
 */
public interface RpcClient<T> {
  

  enum State {

    LATENT, INITIALIZED, STARTED, STOPPED;

  }

  void initialize(ClientOptions clientOptions);

  void start();

  void stop();
  
  State getState();

  public T getRemotingService(Class<? extends T> type, String preferredZone, boolean usedTls, int timeout);

}
