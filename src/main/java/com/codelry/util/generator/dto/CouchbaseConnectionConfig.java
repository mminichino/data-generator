package com.codelry.util.generator.dto;

public class CouchbaseConnectionConfig {
  private String host = "localhost";
  private String username = "";
  private String password = "";
  private String bucket = "default";
  private String scope = "_default";
  private String collection = "_default";
  private boolean useTls = false;
  private boolean tlsSkipVerify = false;

  public String getHost() { return host; }
  public void setHost(String host) { this.host = host; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getBucket() { return bucket; }
  public void setBucket(String bucket) { this.bucket = bucket; }

  public String getScope() { return scope; }
  public void setScope(String scope) { this.scope = scope; }

  public String getCollection() { return collection; }
  public void setCollection(String collection) { this.collection = collection; }

  public boolean isUseTls() { return useTls; }
  public void setUseTls(boolean useTls) { this.useTls = useTls; }

  public boolean isTlsSkipVerify() { return tlsSkipVerify; }
  public void setTlsSkipVerify(boolean tlsSkipVerify) { this.tlsSkipVerify = tlsSkipVerify; }
}
