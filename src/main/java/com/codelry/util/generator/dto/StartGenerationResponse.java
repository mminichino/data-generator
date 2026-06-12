package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartGenerationResponse {

  @JsonProperty("jobId")
  private String jobId;

  @JsonProperty("status")
  private GenerationStatus status;

  @JsonProperty("totalRecords")
  private long totalRecords;

  public StartGenerationResponse() {}

  public StartGenerationResponse(String jobId, GenerationStatus status, long totalRecords) {
    this.jobId = jobId;
    this.status = status;
    this.totalRecords = totalRecords;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public GenerationStatus getStatus() {
    return status;
  }

  public void setStatus(GenerationStatus status) {
    this.status = status;
  }

  public long getTotalRecords() {
    return totalRecords;
  }

  public void setTotalRecords(long totalRecords) {
    this.totalRecords = totalRecords;
  }
}
