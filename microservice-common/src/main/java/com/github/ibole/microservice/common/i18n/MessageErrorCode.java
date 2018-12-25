package com.github.ibole.microservice.common.i18n;


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
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public class MessageErrorCode extends NLS {

  private static final String BUNDLE_NAME = "messages.errorcode";
  //General error message key
  public static String ERROR_FUNCTION_KEY;
  public static String ERROR_PERMISSION_DENIED_KEY;
  public static String ERROR_UNAUTHENTICATED_KEY;
  public static String ERROR_INTERNAL_KEY;
  public static String ERROR_UNAVAILABLE_KEY;
  public static String ERROR_UNKNOWN_KEY;
  //Specific error message key
  public static String CLIENT_ID_REQUIRED_KEY;
  //Token authenticated
  public static String ACCESS_TOKEN_RENEW_FAILED_KEY;

  static {
    // initialize resource bundles
    NLS.initializeMessages(BUNDLE_NAME, MessageErrorCode.class);
  }

}
