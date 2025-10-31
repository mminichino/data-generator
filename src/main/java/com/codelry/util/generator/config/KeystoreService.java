package com.codelry.util.generator.config;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class KeystoreService {

  public static final String KEY_ALIAS = "server-cert";
  public static final String KEYSTORE_TYPE = "PKCS12";
  private final char[] keystorePassword;

  public KeystoreService(
      @Value("${app.ssl.keystore.password:password}") String keystorePassword
  ) {
    this.keystorePassword = keystorePassword.toCharArray();
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  public byte[] ensureAndGetKeystoreBytes() throws Exception {
    KeyStore ks = createSelfSignedKeystore();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ks.store(baos, keystorePassword);
    return baos.toByteArray();
  }

  private KeyStore createSelfSignedKeystore() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();

    X500Name subject = new X500Name("CN=localhost, OU=Gateway, O=Example, L=Local, ST=NA, C=US");
    Instant now = Instant.now();
    Date notBefore = Date.from(now.minus(1, ChronoUnit.DAYS));
    Date notAfter = Date.from(now.plus(3650, ChronoUnit.DAYS));

    X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
        subject,
        java.math.BigInteger.valueOf(now.toEpochMilli()),
        notBefore,
        notAfter,
        subject,
        org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded())
    );

    GeneralName[] sans = getGeneralNames();

    certBuilder.addExtension(Extension.subjectAlternativeName, false, new GeneralNames(sans));

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
        .setProvider("BC").build(keyPair.getPrivate());

    X509CertificateHolder holder = certBuilder.build(signer);
    X509Certificate cert = new JcaX509CertificateConverter()
        .setProvider("BC").getCertificate(holder);
    cert.verify(keyPair.getPublic());

    KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
    ks.load(null, null);
    ks.setKeyEntry(KEY_ALIAS, keyPair.getPrivate(), keystorePassword, new java.security.cert.Certificate[]{cert});
    return ks;
  }

  private static GeneralName[] getGeneralNames() {
    GeneralName[] sans = new GeneralName[] {
        new GeneralName(GeneralName.dNSName, "localhost"),
        new GeneralName(GeneralName.iPAddress, "127.0.0.1")
    };
    try {
      String host = InetAddress.getLocalHost().getHostName();
      if (host != null && !host.isBlank()) {
        sans = new GeneralName[] {
            new GeneralName(GeneralName.dNSName, "localhost"),
            new GeneralName(GeneralName.iPAddress, "127.0.0.1"),
            new GeneralName(GeneralName.dNSName, host)
        };
      }
    } catch (Exception ignored) {}
    return sans;
  }
}
