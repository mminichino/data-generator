package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerateResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("result")
  private String result;

  @JsonProperty("timestamp")
  private long timestamp;

  public GenerateResponse() {
    this.timestamp = System.currentTimeMillis();
  }

  public GenerateResponse(String status, String result) {
    this.status = status;
    this.result = result;
    this.timestamp = System.currentTimeMillis();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
