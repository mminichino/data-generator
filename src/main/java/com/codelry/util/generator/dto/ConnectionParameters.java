package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionParameters {
  private String type = "redis";
  private String hostname = "localhost";
  private int port = 6379;
  private String username = "";
  private String password = "";
  private String database = "";
  private String schema = "";
  private boolean useSsl = false;
  private boolean useJson = false;
  private boolean tlsSkipVerify = false;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("hostname")
  public String getHost() {
    return hostname;
  }

  @JsonProperty("hostname")
  public void setHost(String host) {
    this.hostname = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  public void setUseSsl(boolean useSsl) {
    this.useSsl = useSsl;
  }

  public boolean isUseJson() {
    return useJson;
  }

  public void setUseJson(boolean useJson) {
    this.useJson = useJson;
  }

  public boolean isTlsSkipVerify() {
    return tlsSkipVerify;
  }

  public void setTlsSkipVerify(boolean tlsSkipVerify) {
    this.tlsSkipVerify = tlsSkipVerify;
  }
}
