package com.codelry.util.generator.dto;

import java.time.Duration;

public class RedisConnectionConfig {
  private String host = "localhost";
  private int port = 6379;
  private String password = "";
  private int database = 0;
  private boolean useSsl = false;
  private boolean useJson = false;
  private String keystorePath = "";
  private String keystorePassword = "";
  private String keystoreType = "PKCS12";
  private String truststorePath = "";
  private String truststorePassword = "";
  private String truststoreType = "PKCS12";
  private boolean sslVerify = true;
  private Duration timeout = Duration.ofMillis(5000);
  private int maxActive = 20;
  private int maxIdle = 10;
  private int minIdle = 2;
  private Duration maxWait = Duration.ofMillis(5000);

  public String getHost() { return host; }
  public void setHost(String host) { this.host = host; }

  public int getPort() { return port; }
  public void setPort(int port) { this.port = port; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public int getDatabase() { return database; }
  public void setDatabase(int database) { this.database = database; }

  public boolean isUseSsl() { return useSsl; }
  public void setUseSsl(boolean useSsl) { this.useSsl = useSsl; }

  public boolean isUseJson() { return useJson; }
  public void setUseJson(boolean useJson) { this.useJson = useJson; }

  public String getKeystorePath() { return keystorePath; }
  public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }

  public String getKeystorePassword() { return keystorePassword; }
  public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }

  public String getKeystoreType() { return keystoreType; }
  public void setKeystoreType(String keystoreType) { this.keystoreType = keystoreType; }

  public String getTruststorePath() { return truststorePath; }
  public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }

  public String getTruststorePassword() { return truststorePassword; }
  public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }

  public String getTruststoreType() { return truststoreType; }
  public void setTruststoreType(String truststoreType) { this.truststoreType = truststoreType; }

  public boolean isSslVerify() { return sslVerify; }
  public void setSslVerify(boolean sslVerify) { this.sslVerify = sslVerify; }

  public Duration getTimeout() { return timeout; }
  public void setTimeout(Duration timeout) { this.timeout = timeout; }

  public int getMaxActive() { return maxActive; }
  public void setMaxActive(int maxActive) { this.maxActive = maxActive; }

  public int getMaxIdle() { return maxIdle; }
  public void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }

  public int getMinIdle() { return minIdle; }
  public void setMinIdle(int minIdle) { this.minIdle = minIdle; }

  public Duration getMaxWait() { return maxWait; }
  public void setMaxWait(Duration maxWait) { this.maxWait = maxWait; }
}
