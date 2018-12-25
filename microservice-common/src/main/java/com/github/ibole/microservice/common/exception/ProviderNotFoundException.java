package com.github.ibole.microservice.common.exception;

/**
 * Provider Not Found Exception for the service locate.
 * @author bwang
 *
 */
public final class ProviderNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1;

  public ProviderNotFoundException(String msg) {
    super(msg);
  }
}
