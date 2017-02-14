/**
 * 
 */
package io.ibole.microservice.rpc.server.grpc;

import io.ibole.microservice.rpc.server.RpcServer;
import io.ibole.microservice.rpc.server.RpcServerProvider;
import io.ibole.microservice.rpc.server.ServerBootstrap;

/**
 * Provider for {@link ServerBootstrap} instance. 
 * @author bwang
 *
 */
public class GrpcServerProvider extends RpcServerProvider {

    /* (non-Javadoc)
     * @see cc.toprank.byd.microservice.rpc.server.RpcServerProvider#isAvailable()
     */
    @Override
    protected boolean isAvailable() {
        
        return true;
    }

    /* (non-Javadoc)
     * @see cc.toprank.byd.microservice.rpc.server.RpcServerProvider#priority()
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
