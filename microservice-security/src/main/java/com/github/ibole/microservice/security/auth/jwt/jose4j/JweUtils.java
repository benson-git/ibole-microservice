package com.github.ibole.microservice.security.auth.jwt.jose4j;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.ibole.microservice.security.auth.jwt.BaseTokenValidationCallback;
import com.github.ibole.microservice.security.auth.jwt.GeneralJwtException;
import com.github.ibole.microservice.security.auth.jwt.JwtConstant;
import com.github.ibole.microservice.security.auth.jwt.JwtObject;
import com.github.ibole.microservice.security.auth.jwt.TokenHandlingException;
import com.github.ibole.microservice.security.auth.jwt.TokenValidationCallback;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey.OutputControlLevel;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*********************************************************************************************.
 * 
 * 
 * <p>Copyright 2016, iBole Inc. All rights reserved.
 * 
 * <p></p>
 *********************************************************************************************/

/**
 * Producing and consuming a signed/encrypted JWT.
 * 
 * @author bwang (chikaiwang@hotmail.com)
 *
 */
public final class JweUtils {
 

  private JweUtils(){
    //nothing to do
  }
  
  
  public static void main(String[] args) throws Exception{
    
    generateECKeyPairFiles("d:/senderJWK.json", "d:/receiverJWK.json");
    PublicJsonWebKey senderJwk = toJsonWebKey("d:/senderJWK.json");
    PublicJsonWebKey receiverJwk = toJsonWebKey("d:/receiverJWK.json");
    
    JwtObject jwt = new JwtObject();
    jwt.setAudience("audience");
    jwt.setClientId("clientId");
    jwt.setIssuer("issuer");
    jwt.setLoginId("loginId");
    jwt.setSubject("subject");
    jwt.setTtlSeconds(5);
    jwt.getRoles().add("admin");
    
    final Stopwatch stopwatch = Stopwatch.createStarted();
    
    String token = createJwtWithECKey(jwt, (EllipticCurveJsonWebKey) senderJwk, (EllipticCurveJsonWebKey) receiverJwk);
    String elapsedString = Long.toString(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    //Thread.currentThread().sleep(4900);
    TokenValidationCallback<JwtClaims> validationCallback = new BaseTokenValidationCallback<JwtClaims>();
    
    validateToken(token, jwt.getClientId(), senderJwk, receiverJwk, validationCallback);
    
    JwtObject newjwt = claimsOfTokenWithoutValidation(token, receiverJwk);

    System.out.println("Spent: "+elapsedString);
    System.out.println(validationCallback.getTokenStatus());
    System.out.println(token);
  }
  
  // 生成秘钥工具类
  //Generate an EC key pair, which will be used for signing and verification of the JWT, wrapped in a JWK.
  public static String[] generateECKeyPair(){
    String[] pair = new String[2];
    try {
      EllipticCurveJsonWebKey senderJwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
      EllipticCurveJsonWebKey receiverJwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
      pair[0] = senderJwk.toJson(OutputControlLevel.INCLUDE_PRIVATE);
      pair[1] = receiverJwk.toJson(OutputControlLevel.INCLUDE_PRIVATE);
    } catch (JoseException ex) {
      throw new GeneralJwtException("Generating jwk error happened", ex);
    }
    return pair;
  }
  
  public static PublicJsonWebKey toJsonWebKey(String jsonFile){
    PublicJsonWebKey jsonWebKey;
    File file = new File(jsonFile);
    BufferedReader reader = null;
    StringBuilder laststr = new StringBuilder();
    try {
     reader = new BufferedReader(new FileReader(file));
     String tempString = null;
     while ((tempString = reader.readLine()) != null) {
        laststr.append(tempString);
     }
     
     jsonWebKey = PublicJsonWebKey.Factory.newPublicJwk(laststr.toString());
     
    } catch (IOException | JoseException ex) {
      throw new GeneralJwtException("Reading Json file error happened", ex);
    } finally {      
      IOUtils.closeQuietly(reader);
    }
    
    return jsonWebKey;
  }
  /**
   * Generate ECKey Pair Files.
   * @param senderFilePath
   * @param receiveFilePath
   */
  public static void generateECKeyPairFiles(String senderFilePath, String receiveFilePath){
    String[] pairs = generateECKeyPair();
    generateJsonFile(senderFilePath, pairs[0]);
    generateJsonFile(receiveFilePath, pairs[1]);
    
  }
  
  private static void generateJsonFile(String filePath, String jsonData) {
    FileWriter fw = null;
    Writer write = null;
    try {
      fw = new FileWriter(filePath);
      write = new PrintWriter(fw);
      write.write(jsonData);
      write.flush();
    } catch (IOException ex) {
      throw new GeneralJwtException("Generating Json file error happened", ex);
    } finally {
      IOUtils.closeQuietly(write);
      IOUtils.closeQuietly(fw);
    }

  }
 
  
  public static JsonObject toJson(String jwtJson) {

      return new Gson().fromJson(jwtJson, JsonObject.class);

  }

  /**
   * Create Jwt.
   * @param claimObj the JwtObject
   * @param senderJwk EllipticCurveJsonWebKey
   * @param receiverJwk EllipticCurveJsonWebKey
   * @return token generated
   * @throws JoseException Exception
   */
  public static String createJwtWithECKey(JwtObject claimObj, EllipticCurveJsonWebKey senderJwk, EllipticCurveJsonWebKey receiverJwk) throws JoseException{
    checkArgument(claimObj != null, "Param cannot be null!");
    // Give the JWK a Key ID (kid): 密钥 id
    senderJwk.setKeyId(claimObj.getLoginId());
    receiverJwk.setKeyId(claimObj.getLoginId());    
    // Create the Claims, which will be the content of the JWT
    NumericDate numericDate = NumericDate.now();
    numericDate.addSeconds(claimObj.getTtlSeconds());
    JwtClaims claims = new JwtClaims();
    claims.setIssuer(claimObj.getIssuer()); 
    claims.setAudience(claimObj.getAudience()); 
    claims.setExpirationTime(numericDate);
    // a unique identifier for the token
    claims.setGeneratedJwtId(); 
    claims.setIssuedAtToNow(); 
    claims.setNotBeforeMinutesInThePast(1);
    claims.setSubject(claimObj.getSubject()); 
    claims.setClaim(JwtConstant.CLIENT_ID, claimObj.getClientId());
    claims.setClaim(JwtConstant.LOGIN_ID, claimObj.getLoginId());
    //multi-valued claims work too and will end up as a JSON array
    claims.setStringListClaim(JwtConstant.ROLE_ID, claimObj.getRoles());
    // A JWT is a JWS and/or a JWE with JSON claims as the payload.
    // In this example it is a JWS nested inside a JWE
    // So we first create a JsonWebSignature object.
    JsonWebSignature jws = new JsonWebSignature();
    // The payload of the JWS is JSON content of the JWT Claims
    jws.setPayload(claims.toJson());
    // The JWT is signed using the sender's private key
    jws.setKey(senderJwk.getPrivateKey());
    // Set the Key ID (kid) header.
    jws.setKeyIdHeaderValue(senderJwk.getKeyId());
    // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);   
    // Sign the JWS and produce the compact serialization, which will be the inner JWT/JWS
    // representation, which is a string consisting of three dot ('.') separated
    // base64url-encoded parts in the form Header.Payload.Signature
    String innerJwt = jws.getCompactSerialization();
    // The outer JWT is a JWE
    JsonWebEncryption jwe = new JsonWebEncryption();
    // The output of the ECDH-ES key agreement will encrypt a randomly generated content encryption key
    jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES_A128KW);
    // The content encryption key is used to encrypt the payload
    // with a composite AES-CBC / HMAC SHA2 encryption algorithm
    String encAlg = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256;
    jwe.setEncryptionMethodHeaderParameter(encAlg);
    // We encrypt to the receiver using their public key
    jwe.setKey(receiverJwk.getPublicKey());
    jwe.setKeyIdHeaderValue(receiverJwk.getKeyId());
    // A nested JWT requires that the cty (Content Type) header be set to "JWT" in the outer JWT
    //在使用嵌套签名或加密时，这个头部参数必须存在；在这种情况下，它的值必须是 "JWT"，来表明这是一个在 JWT 中嵌套的 JWT。
    jwe.setContentTypeHeaderValue("JWT");
    // The inner JWT is the payload of the outer JWT
    jwe.setPayload(innerJwt);
    // Produce the JWE compact serialization, which is the complete JWT/JWE representation,
    // which is a string consisting of five dot ('.') separated
    // base64url-encoded parts in the form Header.EncryptedKey.IV.Ciphertext.AuthenticationTag
    return jwe.getCompactSerialization();
  }
  
  public static boolean isExpired(String token, String audience, PublicJsonWebKey pSenderJwk, PublicJsonWebKey pReceiverJwk){

    JwtConsumer secondPassJwtConsumer  = new JwtConsumerBuilder()
            .setRequireExpirationTime() // the JWT must have an expiration time
            .setMaxFutureValidityInMinutes(60)
            .setAllowedClockSkewInSeconds(10) // allow some leeway in validating time based claims to account for clock skew
            .setDecryptionKey(pReceiverJwk.getPrivateKey()) // decrypt with the receiver's private key
            .setVerificationKey(pSenderJwk.getPublicKey()) // verify the signature with the sender's public key
            //.registerValidator(new ClientIdentifierValidator(clientId))
            .setExpectedAudience(audience)
            .build(); // create the JwtConsumer instance
        
        try {
            secondPassJwtConsumer.processToClaims(token);
        } catch (InvalidJwtException e) {
            return true;
        }
        return false;
  }
  
  /**
   * Validate Token based on the provided token.
   * 
   * <pre>
   * 1. validate the token signature
   * 2. check if the token is for the same client identifier
   * 3. check if the token is expired
   * @param token String
   * @param clientId String
   * @param senderJwk PublicJsonWebKey
   * @param receiverJwk PublicJsonWebKey
   * @param validationCallback TokenValidationCallback
   */
  public static void validateToken(String token, String clientId, PublicJsonWebKey senderJwk, PublicJsonWebKey receiverJwk,
      TokenValidationCallback<JwtClaims> validationCallback) {
    JwtClaims claims = null;
    try {
      // validate the signature
      claims = validateSignature(token, senderJwk, receiverJwk);
      if (claims == null) {
        validationCallback.onInValid(claims);
        return;
      }
      // validate the client identifier
      if (!claims.getClaimValue(JwtConstant.CLIENT_ID).equals(clientId)) {
        validationCallback.onInValid(claims);
        return;
      }
      // validate the expiration time
      if (claims.getExpirationTime().isBefore(NumericDate.now())) {
        validationCallback.onExpired(claims);
        return;
      }

    } catch (TokenHandlingException | MalformedClaimException e) {
      validationCallback.onError(claims);
    }
    validationCallback.onValiated(claims);
  }
  
  private static JwtClaims validateSignature(String token, PublicJsonWebKey senderJwk, PublicJsonWebKey receiverJwk) throws TokenHandlingException{
    
    JwtClaims claims = null;
    // Validate token signature
    JsonWebSignature jws = new JsonWebSignature();
    try {
      JsonWebEncryption jwe = new JsonWebEncryption();
      jwe.setKey(receiverJwk.getPrivateKey());
      jwe.setCompactSerialization(token);
      jws.setCompactSerialization(jwe.getPayload());
      // Give the JWK a Key ID (kid): 密钥 id
      jws.setKey(senderJwk.getPublicKey());
      if (jws.verifySignature()) {
        claims = JwtClaims.parse(jws.getPayload());
      }
    } catch (JoseException | InvalidJwtException ex) {
         throw new TokenHandlingException(ex);
    }
    return claims;
  }
  /**
   * Restore the JwtObject from the provided token.
   * @param token String
   * @param clientId String
   * @param receiverJwk PublicJsonWebKey
   * @return JwtObject JwtObject
   * @throws TokenHandlingException Exception
   */
  @SuppressWarnings("unchecked")
  public static JwtObject claimsOfTokenWithoutValidation(String token, PublicJsonWebKey receiverJwk) throws TokenHandlingException {
    
    JwtObject jwtObj = null;
    try {

      JwtContext jwtctx = parseJwt(token, receiverJwk);
      jwtObj = new JwtObject();
      if (jwtctx.getJwtClaims().getAudience() != null
          && jwtctx.getJwtClaims().getAudience().size() > 0) {
        jwtObj.setAudience(jwtctx.getJwtClaims().getAudience().get(0));
      }
      jwtObj.setIssuer(jwtctx.getJwtClaims().getIssuer());
      jwtObj.setClientId(jwtctx.getJwtClaims().getStringClaimValue(JwtConstant.CLIENT_ID));
      jwtObj.setLoginId(jwtctx.getJwtClaims().getStringClaimValue(JwtConstant.LOGIN_ID));
      jwtObj.setSubject(jwtctx.getJwtClaims().getSubject());
      jwtObj.getRoles()
          .addAll((List<String>) jwtctx.getJwtClaims().getClaimsMap().get(JwtConstant.ROLE_ID));

    } catch (MalformedClaimException | InvalidJwtException ex) {
      throw new TokenHandlingException(ex);
    }
    return jwtObj;
  }


  /**
   * Parse Jwt with public JWK.
   * @param token the token to parse
   * @param receiverJwk PublicJsonWebKey
   * @return JwtContext
   * @throws InvalidJwtException
   */
  private static JwtContext parseJwt(String token, PublicJsonWebKey receiverJwk) throws InvalidJwtException
       {
    // Build a JwtConsumer that doesn't check signatures or do any validation.
    JwtConsumer firstPassJwtConsumer = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .setDecryptionKey(receiverJwk.getPrivateKey())
            .build();

    //The first JwtConsumer is basically just used to parse the JWT into a JwtContext object.
    JwtContext jwtContext = firstPassJwtConsumer.process(token);
    return jwtContext;
  }
}