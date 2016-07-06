/**
 * 
 */
package practices.microservice.demo.service.edge;

import org.springframework.stereotype.Component;

import io.grpc.stub.StreamObserver;
import practices.microservice.demo.protos.GreeterGrpc;
import practices.microservice.demo.protos.Helloworld.HelloReply;
import practices.microservice.demo.protos.Helloworld.HelloRequest;

/**
 * @author bwang
 *
 */
@Component
public class GreeterEdgeService extends GreeterGrpc.AbstractGreeter {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }
