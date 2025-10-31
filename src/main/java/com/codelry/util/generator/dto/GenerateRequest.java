package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerateRequest {

  @JsonProperty("count")
  private int count;

  @JsonProperty("schema")
  private java.util.Map<String, Object> schema;

  @JsonProperty("connection")
  private java.util.Map<String, Object> connection;

  public GenerateRequest() {
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public java.util.Map<String, Object> getSchema() {
    return schema;
  }

  public void setSchema(java.util.Map<String, Object> schema) {
    this.schema = schema;
  }
}
