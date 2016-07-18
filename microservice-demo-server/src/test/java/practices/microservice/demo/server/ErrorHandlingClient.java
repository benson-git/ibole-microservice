package practices.microservice.demo.server;

	import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import practices.microservice.demo.protos.ErrorReplyOuterClass.ErrorReply;
import practices.microservice.demo.protos.GreeterGrpc;
import practices.microservice.demo.protos.GreeterGrpc.GreeterBlockingStub;
import practices.microservice.demo.protos.GreeterGrpc.GreeterFutureStub;
import practices.microservice.demo.protos.GreeterGrpc.GreeterStub;
import practices.microservice.demo.protos.Helloworld.HelloReply;
import practices.microservice.demo.protos.Helloworld.HelloRequest;

import com.google.common.base.Verify;
import com.google.common.base.VerifyException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.InvalidProtocolBufferException;

	/**
	 * Shows how to extract error information from a server response.
	 */
	public class ErrorHandlingClient {
	  static final Metadata.Key<byte[]>
	      ERROR_REPLY_HEADER = Metadata.Key.of("error-reply-bin", Metadata.BINARY_BYTE_MARSHALLER);
	  static final ErrorReply ERROR_REPLY =
	      ErrorReply.newBuilder().setDetail("detailed error info.").build();

	  public static void main(String[] args) throws Exception {
	    new ErrorHandlingClient().run();
	  }

	  private Server server;
	  private ManagedChannel channel;

	  void run() throws Exception {
	    server = ServerBuilder.forPort(0).addService(new GreeterGrpc.GreeterImplBase() {
	      @Override
	      public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
	        Metadata trailers = new Metadata();
	        trailers.put(ERROR_REPLY_HEADER, ERROR_REPLY.toByteArray());

	        responseObserver.onError(Status.INTERNAL.asRuntimeException(trailers));
	      }
	    }).build().start();
	    channel =
	        ManagedChannelBuilder.forAddress("localhost", server.getPort()).usePlaintext(true).build();

	    blockingCall();
	    futureCallDirect();
	    futureCallCallback();
	    asyncCall();
	    advancedAsyncCall();

	    channel.shutdown();
	    server.shutdown();
	    channel.awaitTermination(1, TimeUnit.SECONDS);
	    server.awaitTermination();
	  }

	  static void verifyErrorReply(Throwable t) {
	    Status status = Status.fromThrowable(t);
	    Metadata trailers = Status.trailersFromThrowable(t);
	    Verify.verify(status.getCode() == Status.Code.INTERNAL);
	    Verify.verify(trailers.containsKey(ERROR_REPLY_HEADER));
	    try {
	      Verify.verify(ErrorReply.parseFrom(trailers.get(ERROR_REPLY_HEADER)).equals(ERROR_REPLY));
	    } catch (InvalidProtocolBufferException i) {
	      throw new VerifyException(i);
	    }
	  }

	  void blockingCall() {
	    GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
	    try {
	      stub.sayHello(HelloRequest.newBuilder().setName("Bart").build());
	    } catch (Exception e) {
	      verifyErrorReply(e);
	    }
	  }

	  void futureCallDirect() {
	    GreeterFutureStub stub = GreeterGrpc.newFutureStub(channel);
	    ListenableFuture<HelloReply> response =
	        stub.sayHello(HelloRequest.newBuilder().setName("Lisa").build());

	    try {
	      response.get();
	    } catch (InterruptedException e) {
	      Thread.currentThread().interrupt();
	      throw new RuntimeException(e);
	    } catch (ExecutionException e) {
	      verifyErrorReply(e.getCause());
	    }
	  }

	  void futureCallCallback() {
	    GreeterFutureStub stub = GreeterGrpc.newFutureStub(channel);
	    ListenableFuture<HelloReply> response =
	        stub.sayHello(HelloRequest.newBuilder().setName("Maggie").build());

	    final CountDownLatch latch = new CountDownLatch(1);

	    Futures.addCallback(response, new FutureCallback<HelloReply>() {
	      @Override
	      public void onSuccess(@Nullable HelloReply result) {
	        // Won't be called, since the server in this example always fails.
	      }

	      @Override
	      public void onFailure(Throwable t) {
	        verifyErrorReply(t);
	        latch.countDown();
	      }
	    });

	    if (!Uninterruptibles.awaitUninterruptibly(latch, 1, TimeUnit.SECONDS)) {
	      throw new RuntimeException("timeout!");
	    }
	  }

	  void asyncCall() {
	    GreeterStub stub = GreeterGrpc.newStub(channel);
	    HelloRequest request = HelloRequest.newBuilder().setName("Homer").build();
	    final CountDownLatch latch = new CountDownLatch(1);
	    StreamObserver<HelloReply> responseObserver = new StreamObserver<HelloReply>() {

	      @Override
	      public void onNext(HelloReply value) {
	        // Won't be called.
	      }

	      @Override
	      public void onError(Throwable t) {
	        verifyErrorReply(t);
	        latch.countDown();
	      }

	      @Override
	      public void onCompleted() {
	        // Won't be called, since the server in this example always fails.
	      }
	    };
	    stub.sayHello(request, responseObserver);

	    if (!Uninterruptibles.awaitUninterruptibly(latch, 1, TimeUnit.SECONDS)) {
	      throw new RuntimeException("timeout!");
	    }
	  }


	  /**
	   * This is more advanced and does not make use of the stub.  You should not normally need to do
	   * this, but here is how you would.
	   */
	  void advancedAsyncCall() {
	    ClientCall<HelloRequest, HelloReply> call =
	        channel.newCall(GreeterGrpc.METHOD_SAY_HELLO, CallOptions.DEFAULT);

	    final CountDownLatch latch = new CountDownLatch(1);

	    call.start(new ClientCall.Listener<HelloReply>() {

	      @Override
	      public void onClose(Status status, Metadata trailers) {
	        Verify.verify(status.getCode() == Status.Code.INTERNAL);
	        Verify.verify(trailers.containsKey(ERROR_REPLY_HEADER));
	        try {
	          Verify.verify(ErrorReply.parseFrom(trailers.get(ERROR_REPLY_HEADER)).equals(ERROR_REPLY));
	        } catch (InvalidProtocolBufferException i) {
	          throw new VerifyException(i);
	        }

	        latch.countDown();
	      }
	    }, new Metadata());

	    call.sendMessage(HelloRequest.newBuilder().setName("Marge").build());
	    call.halfClose();

	    if (!Uninterruptibles.awaitUninterruptibly(latch, 1, TimeUnit.SECONDS)) {
	      throw new RuntimeException("timeout!");
	    }
	  }
	}
