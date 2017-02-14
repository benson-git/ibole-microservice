package io.ibole.microservice.config.rpc.client;

import io.ibole.microservice.common.ServerIdentifier;

/**
 * The interface of RPC client.
 * 
 * @author bwang
 *
 */
public interface RpcClient<T> {

  void initialize(ServerIdentifier identifier);

  void start();

  void stop();

  public T getRemotingService(Class<? extends T> type, int timeout);

}
