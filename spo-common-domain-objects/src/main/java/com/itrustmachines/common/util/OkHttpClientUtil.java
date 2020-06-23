package com.itrustmachines.common.util;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.*;

import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;

@UtilityClass
@Slf4j
public class OkHttpClientUtil {
  
  @Synchronized
  public OkHttpClient getOkHttpClient() {
    final OkHttpClient client = ignoreCertificate().build();
    log.info("getOkHttpClient() result={}", client);
    return client;
  }
  
  @Synchronized
  public OkHttpClient getOkHttpClient(String userName, String password) {
    final OkHttpClient client = authenticate(userName, password).build();
    log.info("getOkHttpClient() result={}", client);
    return client;
  }
  
  private OkHttpClient.Builder ignoreCertificate() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder = configureToIgnoreCertificate(builder);
    builder = builder.readTimeout(30, TimeUnit.SECONDS);
    return builder;
  }
  
  private OkHttpClient.Builder configureToIgnoreCertificate(OkHttpClient.Builder builder) {
    log.info("Ignore Ssl Certificate");
    try {
      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {
        }
        
        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {
        }
        
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[] {};
        }
      } };
      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
      builder.hostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });
    } catch (Exception e) {
      log.error("Exception while configuring IgnoreSslCertificate", e);
    }
    return builder;
  }
  
  // ignore ssl and need authenticate
  private OkHttpClient.Builder authenticate(String userName, String password) {
    OkHttpClient.Builder builder = ignoreCertificate();
    builder = builder.authenticator((route, response) -> {
      String credential = Credentials.basic(userName, password);
      return response.request()
                     .newBuilder()
                     .header("Authorization", credential)
                     .build();
    });
    return builder;
  }
  
}
