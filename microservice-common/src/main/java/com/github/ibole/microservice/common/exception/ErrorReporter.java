package com.github.ibole.microservice.common.exception;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.ibole.microservice.common.exception.ErrorDetailsProto.ErrorDetails;
import com.github.ibole.microservice.common.exception.ErrorDetailsProto.ErrorDetails.Builder;
import com.github.ibole.microservice.common.utils.EqualsUtil;
import com.github.ibole.microservice.common.utils.HashCodeUtil;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/


/**
 *  Defines the status of an operation by providing a standard {@link GeneralErrorCode} 
 *  in conjunction with an optional specific error code.
 *    
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class ErrorReporter {

  // Create the canonical list of ErrorStatus instances indexed by their code values.
  private static final Map<String, ErrorReporter> STATUS_MAP = buildStatusMap();

  public static final ErrorReporter FUNCTION = GeneralErrorCode.FUNCTION.toStatus();
  
  public static final ErrorReporter PERMISSION_DENIED = GeneralErrorCode.PERMISSION_DENIED
      .toStatus();

  public static final ErrorReporter UNAUTHENTICATED = GeneralErrorCode.UNAUTHENTICATED.toStatus();

  public static final ErrorReporter INTERNAL = GeneralErrorCode.INTERNAL.toStatus();
  
  public static final ErrorReporter UNAVAILABLE = GeneralErrorCode.UNAVAILABLE.toStatus();
  
  public static final ErrorReporter UNKNOWN = GeneralErrorCode.UNKNOWN.toStatus();

  private final GeneralErrorCode code;
  
  private final String specificErrorCode;
  
  private final String specificErrorMsg;
  
  private final Throwable cause;

  private ErrorReporter(GeneralErrorCode code) {
    this(code, null, null, null);
  }

  private ErrorReporter(GeneralErrorCode code, String specificErrorCode, String specificErrorMsg, Throwable cause) {
    this.code = checkNotNull(code);
    this.specificErrorCode = specificErrorCode;
    this.specificErrorMsg = specificErrorMsg;
    this.cause = cause;
  }

  /**
   * Create a derived instance of {@link ErrorReporter} with the given cause.
   * However, the cause is not transmitted from server to client.
   * @param cause the cause of throwable
   * @return the instance of ErrorReporter
   */
  public ErrorReporter withCause(Throwable cause) {
    if (Objects.equal(this.cause, cause)) {
      return this;
    }
    return new ErrorReporter(this.code, this.specificErrorCode, this.specificErrorMsg, cause);
  }

  /**
   * Create a derived instance of {@link ErrorReporter} with the given specific error massage.  
   * @param specificErrorMsg the cause of throwable
   * @return the instance of ErrorReporter
   */
  public ErrorReporter withErrorMsg(String specificErrorMsg) {
    if (Objects.equal(this.specificErrorMsg, specificErrorMsg)) {
      return this;
    }
    return new ErrorReporter(this.code, this.specificErrorCode, specificErrorMsg, this.cause);
  }
  
  /**
   * Create a derived instance of {@link ErrorReporter} with the given specific error massage.  
   * <code>includingErrorCode = true</code> means the error message content is prefixed with error code,
   *  Example: ERROR_FUNCTION=ERROR_FUNCTION:Unsupport business operation.
   * @param specificErrorMsg the cause of throwable
   * @param includingErrorCode if include error code
   * @return the instance of ErrorReporter
   */
  public ErrorReporter withErrorMsg(String specificErrorMsg, boolean includingErrorCode) {

    String errorCode = this.specificErrorCode;
    int index = specificErrorMsg.indexOf(':');
    if (includingErrorCode && index > 0) {
      errorCode = specificErrorMsg.substring(0, index);
    }
    return new ErrorReporter(this.code, errorCode, specificErrorMsg, this.cause);
  }
  
  /**
   * Create a derived instance of {@link ErrorReporter} with the given specific error code.
   * @param specificErrorCode the error code
   * @return the instance of ErrorReporter
   * 
   */
  public ErrorReporter withErrorCode(String specificErrorCode) {
    
    if (Objects.equal(this.specificErrorCode, specificErrorCode)) {
      return this;
    }
    
    return new ErrorReporter(this.code, specificErrorCode, this.specificErrorMsg, this.cause);
  }
  
  public GeneralErrorCode getGeneralCode() {
    return this.code;
  }
  
  /**
   * Build to ErrorDetails.
   * @return the error details ErrorDetails
   */
  public ErrorDetails toErrorDetails() {

    Builder builder = ErrorDetails.newBuilder().setGeneralCode(this.code.value);
    List<String> detailedErrors = Lists.newArrayList();
    if (!Strings.isNullOrEmpty(this.specificErrorCode)) {
      builder.setSpecificCode(this.specificErrorCode);
    }   
    if (!Strings.isNullOrEmpty(this.specificErrorMsg)) {
      builder.setSpecificMessage(this.specificErrorMsg);
    }
    if (null != this.cause) {
      detailedErrors.add(ExceptionUtils.getRootCauseMessage(cause));
    }
    builder.addAllDetailedMessage(detailedErrors);
    return builder.build();
  }
  
  public String getSpecificCode() {
    return this.specificErrorCode;
  }
  
  
  public String getSpecificErrorMsg() {
    return this.specificErrorMsg;
  }
  
  public Throwable getCause() {
    return this.cause;
  }

  /** A string representation of the status useful for debugging. */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("code", code.name())
        .add("specificCode", specificErrorCode)
        .add("specificErrorMsg", specificErrorMsg)
        .add("cause", cause)
        .toString();
  }
  
  /**
   * Indicates whether some other object is "equal to" this {@code ErrorStatus}. The result is
   * {@code true} if and only if the argument is not {@code null} and is a {@code ErrorStatus}
   * object that represents the same label (case-sensitively) as this {@code ErrorStatus}.
   * 
   * @param obj the reference object with which to compare
   * @return {@code true} if this {@code ErrorStatus} is the same as {@code obj}; {@code false}
   *         otherwise
   * @see #hashCode()
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ErrorReporter)) {
      return false;
    }
    final ErrorReporter other = (ErrorReporter) obj;
    return EqualsUtil.equal(this.code, other.code)
        && EqualsUtil.equal(this.specificErrorCode, other.specificErrorCode);
  }

  /**
   * Returns a hash code for this {@code ErrorStatus}.
   * 
   * <p>This implementation is consistent with {@code equals}.
   * 
   * @return a hash code for this {@code ErrorStatus}
   * @see #equals(Object)
   */
  @Override
  public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, this.code);
    result = HashCodeUtil.hash(result, this.specificErrorCode);
    return result;
  }

  private static Map<String, ErrorReporter> buildStatusMap() {
    Map<String, ErrorReporter> canonicalizer = new HashMap<String, ErrorReporter>();
    for (GeneralErrorCode code : GeneralErrorCode.values()) {
      ErrorReporter replaced = canonicalizer.put(code.value(), new ErrorReporter(code));
      if (replaced != null) {
        throw new IllegalStateException("Code value duplication between "
            + replaced.getGeneralCode().name() + " & " + code.name());
      }
    }
    return canonicalizer;
  }
  
  public enum GeneralErrorCode {

    /**
     * Incorrect/unsupport business operation.
     */
    FUNCTION("ERROR_FUNCTION"),
    /**
     * The caller does not have permission to execute the specified operation. PERMISSION_DENIED
     * must not be used if the caller cannot be identified (use UNAUTHENTICATED instead for those
     * errors).
     */
    PERMISSION_DENIED("ERROR_PERMISSION_DENIED"),
    /**
     * The request does not have valid authentication credentials for the operation.
     */
    UNAUTHENTICATED("ERROR_UNAUTHENTICATED"),
    /**
     * Internal errors. Means some invariants expected by underlying system has been broken. If you
     * see one of these errors, something is very broken.
     */
    INTERNAL("ERROR_INTERNAL"),
    /**
     * The service is currently unavailable.
     */
    UNAVAILABLE("ERROR_UNAVAILABLE"),
    /**
     * Unknown error. Example, if errors raised by APIs that do not return enough error information
     * may be converted to this error.
     */
    UNKNOWN("ERROR_UNKNOWN");

    private final String value;

    private GeneralErrorCode(String value) {
      this.value = value;
    }

    /**
     * The value of the status.
     * @return the enum value
     */
    public String value() {
      return value;
    }

    public ErrorReporter toStatus() {
      return STATUS_MAP.get(value);
    }

  }
}
