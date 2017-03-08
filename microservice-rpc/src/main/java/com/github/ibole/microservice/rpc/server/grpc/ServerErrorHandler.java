package com.github.ibole.microservice.rpc.server.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import com.github.ibole.infrastructure.common.exception.ErrorDetailsProto.ErrorDetails;
import com.github.ibole.infrastructure.common.exception.ErrorReporter;


/*********************************************************************************************
 * .
 * 
 * 
 * <p>
 * Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


public class ServerErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(ServerErrorHandler.class.getName());

  private static Metadata.Key<ErrorDetails> errorDetailsKey =
      ProtoUtils.keyForProto(ErrorDetails.getDefaultInstance());

  /**
   * Used in service methods to pass appropriate exception to responseObserver. Also logs errors
   * that aren't due to client errors (e.g. invalid argument, etc).
   */
  public static <V> void handleError(ErrorReporter errorReport, StreamObserver<V> responseObserver) {

    if (errorReport.getGeneralCode().equals(ErrorReporter.FUNCTION)) {
      logger.error("Service error happened, error code '{}', error message '{}' ",
          errorReport.getSpecificCode(), errorReport.getSpecificErrorMsg(), errorReport.getCause());
    }
    responseObserver.onError(toException(errorReport));
  }

  private static Exception toException(ErrorReporter errorReport) {
    Metadata trailers = new Metadata();
    trailers.put(errorDetailsKey, errorReport.toErrorDetails());
    switch (errorReport.getGeneralCode()) {
      case FUNCTION:
        return Status.FAILED_PRECONDITION.withCause(errorReport.getCause())
            .withDescription(errorReport.getSpecificErrorMsg()).asException(trailers);
      case UNAVAILABLE:
        return Status.UNAVAILABLE.withCause(errorReport.getCause())
            .withDescription(errorReport.getSpecificErrorMsg()).asRuntimeException(trailers);
      case INTERNAL:
        return Status.INTERNAL.withCause(errorReport.getCause())
            .withDescription(errorReport.getSpecificErrorMsg()).asRuntimeException(trailers);
      default:
        return Status.UNKNOWN.withCause(errorReport.getCause())
            .withDescription(errorReport.getSpecificErrorMsg()).asRuntimeException(trailers);
    }
  }
}
