package com.codelry.util.generator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class HttpsConnectorConfig {

  private final KeystoreService keystoreService;
  private final char[] keystorePassword;
  private final Integer httpsPort;

  public HttpsConnectorConfig(
      KeystoreService keystoreService,
      @Value("${server.ssl.key-store-password:password}") String keystorePassword,
      @Value("${server.https.port:1443}") Integer httpsPort
  ) {
    this.keystoreService = keystoreService;
    this.keystorePassword = keystorePassword.toCharArray();
    this.httpsPort = httpsPort;
  }

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> sslCustomizer() {
    return factory -> {
      try {
        byte[] keystoreBytes = keystoreService.ensureAndGetKeystoreBytes();

        Path tempKs = Files.createTempFile("server-https-keystore", ".p12");
        Files.write(tempKs, keystoreBytes);
        tempKs.toFile().deleteOnExit();

        Ssl ssl = new Ssl();
        ssl.setEnabled(true);
        ssl.setKeyAlias(KeystoreService.KEY_ALIAS);
        ssl.setKeyStoreType(KeystoreService.KEYSTORE_TYPE);
        ssl.setKeyStorePassword(new String(keystorePassword));
        ssl.setKeyStore(tempKs.toUri().toString());

        factory.setSsl(ssl);
        factory.setPort(httpsPort != null ? httpsPort : 443);
      } catch (Exception e) {
        throw new IllegalStateException("Failed to configure HTTPS keystore", e);
      }
    };
  }
}
