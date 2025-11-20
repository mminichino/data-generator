package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class GenerateResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("result")
  private String result;

  @JsonProperty("timestamp")
  private long timestamp;

  @JsonProperty("samples")
  private List<Map<String, Object>> samples;

  public GenerateResponse() {
    this.timestamp = System.currentTimeMillis();
  }

  public GenerateResponse(String status, String result) {
    this.status = status;
    this.result = result;
    this.timestamp = System.currentTimeMillis();
  }

  public GenerateResponse(String status, String result, List<Map<String, Object>> samples) {
    this.status = status;
    this.result = result;
    this.samples = samples;
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

  public List<Map<String, Object>> getSamples() {
    return samples;
  }

  public void setSamples(List<Map<String, Object>> samples) {
    this.samples = samples;
  }
}
