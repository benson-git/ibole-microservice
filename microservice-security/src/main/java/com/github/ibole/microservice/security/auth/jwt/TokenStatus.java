package com.github.ibole.microservice.security.auth.jwt;

import java.util.HashMap;
import java.util.Map;

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


public final class TokenStatus {

  // Create the canonical list of TokenStatus instances indexed by their code values.
  private static final Map<String, TokenStatus> STATUS_MAP = buildStatusMap();

  public static final TokenStatus EXPIRED = Code.EXPIRED.toStatus();

  public static final TokenStatus INVALID = Code.INVALID.toStatus();

  public static final TokenStatus VALIDATED = Code.VALIDATED.toStatus();

  private final Code code;

  public TokenStatus(Code code) {
    this.code = code;
  }


  public Code getCode() {
    return this.code;
  }
  
  public boolean isInvalid() {
    return Code.INVALID.equals(this.code);
  }
  
  public boolean isValidated() {
    return Code.VALIDATED.equals(this.code);
  }
  
  public boolean isExpired() {
    return Code.EXPIRED.equals(this.code);
  }

  @Override
  public String toString() {
    return "Status:" + code;
  }

  private static Map<String, TokenStatus> buildStatusMap() {
    Map<String, TokenStatus> canonicalizer = new HashMap<String, TokenStatus>();
    for (Code code : Code.values()) {
      TokenStatus replaced = canonicalizer.put(code.value(), new TokenStatus(code));
      if (replaced != null) {
        throw new IllegalStateException(
            "Code value duplication between " + replaced.getCode().name() + " & " + code.name());
      }
    }
    return canonicalizer;
  }
  
  public enum Code {

    /**
     * The token is expired.
     */
    EXPIRED("EXPIRED"),
    /**
     * The token is illegal, invalid signature or invalid client identifier.
     */
    INVALID("INVALID"),
    /**
     * The token is validated.
     */
    VALIDATED("VALIDATED");

    private final String value;

    private Code(String value) {
      this.value = value;
    }

    /**
     * The value of the status.
     */
    public String value() {
      return value;
    }

    public TokenStatus toStatus() {
      return STATUS_MAP.get(value);
    }

  }

}
