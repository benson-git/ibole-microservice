package com.github.ibole.microservice.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

public final class SslUtils {

  private SslUtils() {}

  /**
   * Saves a file from the classpath resources in src/main/resources/certs as a file on the
   * filesystem.
   *
   * @param name name of a file in src/main/resources/certs.
   * @return cert file File
   * @throws IOException if the I/O exception happen
   */
  public static File loadCert(String name) throws IOException {
    InputStream in = SslUtils.class.getResourceAsStream("/certs/" + name);
    File tmpFile = File.createTempFile(name, "");
    tmpFile.deleteOnExit();

    BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
    try {
      int b;
      while ((b = in.read()) != -1) {
        writer.write(b);
      }
    } finally {
      writer.close();
    }

    return tmpFile;
  }

  /**
   * Loads an X.509 certificate from the classpath resources in src/main/resources/certs.
   *
   * @param fileName name of a file in src/main/resources/certs.
   * @return the instance of X509Certificate
   * @throws CertificateException if certificate exception happen
   * @throws IOException if I/O exception happen
   */
  public static X509Certificate loadX509Cert(String fileName)
      throws CertificateException, IOException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    InputStream in = SslUtils.class.getResourceAsStream("/certs/" + fileName);
    try {
      return (X509Certificate) cf.generateCertificate(in);
    } finally {
      in.close();
    }
  }

  /**
   * Creates an SSLSocketFactory which contains {@code certChainFile} as its only root certificate.
   * @param certChainFile File
   * @return the instance of SSLSocketFactory
   * @throws Exception Exception
   */
  public static SSLSocketFactory newSslSocketFactoryForCa(File certChainFile) throws Exception {
    InputStream is = new FileInputStream(certChainFile);
    try {
      return newSslSocketFactoryForCa(is);
    } finally {
      is.close();
    }
  }

  /**
   * Creates an SSLSocketFactory which contains {@code certChainFile} as its only root certificate.
   * @param certChain InputStream
   * @return the instance of SSLSocketFactory
   * @throws Exception Exception
   */
  public static SSLSocketFactory newSslSocketFactoryForCa(InputStream certChain) throws Exception {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert =
        (X509Certificate) cf.generateCertificate(new BufferedInputStream(certChain));
    X500Principal principal = cert.getSubjectX500Principal();
    ks.setCertificateEntry(principal.getName("RFC2253"), cert);

    // Set up trust manager factory to use our key store.
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(ks);
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, trustManagerFactory.getTrustManagers(), null);
    return context.getSocketFactory();
  }
}
