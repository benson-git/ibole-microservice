package io.ibole.microservice.rpc.client.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.ibole.infrastructure.common.exception.ErrorDetailsProto.ErrorDetails;
import io.ibole.infrastructure.common.exception.ErrorReporter;


/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * 版权所有：(c)2016， 深圳市拓润计算机软件开发有限公司
 * 
 * <p>
 * </p>
 *********************************************************************************************/


public class ClientErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(ClientErrorHandler.class.getName());

  private static Metadata.Key<ErrorDetails> errorDetailsKey =
      ProtoUtils.keyForProto(ErrorDetails.getDefaultInstance());

  /**
   * Used in service methods to pass appropriate exception to responseObserver. Also logs errors
   * that aren't due to client errors (e.g. invalid argument, etc).
   */
  public static ErrorReporter handleError(Exception ex) {

     logger.error("Calling remoting service error happened", ex);
     return toErrorReporter(ex);
  }

  private static ErrorReporter toErrorReporter(Exception ex) {
    Metadata trailers = Status.trailersFromThrowable(ex);
    ErrorDetails errorDetails = trailers.get(errorDetailsKey);
    Status status = Status.fromThrowable(ex);
    Throwable throwable = null;
    if(errorDetails.getDetailedMessageList().size() > 0){
      throwable = new Throwable(errorDetails.getDetailedMessage(0));
    }
    switch (status.getCode()) {
      case FAILED_PRECONDITION:
        return ErrorReporter.FUNCTION.withCause(throwable)
            .withSpecificErrorCode(errorDetails.getSpecificCode())
            .withSpecificErrorMsg(errorDetails.getSpecificMessage());
      case UNAVAILABLE:
        return ErrorReporter.UNAVAILABLE.withCause(throwable)
            .withSpecificErrorCode(errorDetails.getSpecificCode())
            .withSpecificErrorMsg(errorDetails.getSpecificMessage());
      case INTERNAL:
        return ErrorReporter.INTERNAL.withCause(throwable)
            .withSpecificErrorCode(errorDetails.getSpecificCode())
            .withSpecificErrorMsg(errorDetails.getSpecificMessage());
      default:
        return ErrorReporter.UNKNOWN.withCause(throwable)
            .withSpecificErrorCode(errorDetails.getSpecificCode())
            .withSpecificErrorMsg(errorDetails.getSpecificMessage());
    }
  }
}
