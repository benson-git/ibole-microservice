/**
 * 
 */
package com.github.ibole.microservice.rpc.server.grpc;

import com.github.ibole.microservice.rpc.server.RpcServer;
import com.github.ibole.microservice.rpc.server.RpcServerProvider;
import com.github.ibole.microservice.rpc.server.ServerBootstrap;

/**
 * Provider for {@link ServerBootstrap} instance. 
 * @author bwang
 *
 */
public class GrpcServerProvider extends RpcServerProvider {

    /* (non-Javadoc)
     * @see com.github.ibole.microservice.rpc.server.RpcServerProvider#isAvailable()
     */
    @Override
    protected boolean isAvailable() {
        
        return true;
    }

    /* (non-Javadoc)
     * @see com.github.ibole.microservice.rpc.server.RpcServerProvider#priority()
     */
    @Override
    protected int priority() {
        
        return 5;
    }

    @Override
    protected RpcServer createServer() {
        
        return new GrpcServer();
    }

}
