package com.github.ibole.microservice.common.exception;

/*********************************************************************************************
 * .
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p>
 * </p>
 *********************************************************************************************/


/**
 * {@link ErrorReporter} in RuntimeException form, for propagating technical error code
 * information via exceptions.
 * 
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class TechnicalException extends RuntimeException {

  private static final long serialVersionUID = -4679245578049447461L;

  private final ErrorReporter errorReporter;

  public TechnicalException(ErrorReporter errorReporter) {
    this.errorReporter = errorReporter;
  }

  public static TechnicalException fromErrorReporter(ErrorReporter errorReporter) {
    return new TechnicalException(errorReporter);
  }

  /**
   * Get Error Reporter.
   * @return the errorReporter
   */
  public ErrorReporter getErrorReporter() {
    return errorReporter;
  }

}
