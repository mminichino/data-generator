package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerationJobStatus {

  @JsonProperty("jobId")
  private String jobId;

  @JsonProperty("status")
  private GenerationStatus status;

  @JsonProperty("totalRecords")
  private long totalRecords;

  @JsonProperty("completedRecords")
  private long completedRecords;

  @JsonProperty("percentComplete")
  private int percentComplete;

  @JsonProperty("recordsPerSecond")
  private double recordsPerSecond;

  @JsonProperty("message")
  private String message;

  @JsonProperty("startedAt")
  private long startedAt;

  @JsonProperty("updatedAt")
  private long updatedAt;

  public GenerationJobStatus() {}

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

  public long getCompletedRecords() {
    return completedRecords;
  }

  public void setCompletedRecords(long completedRecords) {
    this.completedRecords = completedRecords;
  }

  public int getPercentComplete() {
    return percentComplete;
  }

  public void setPercentComplete(int percentComplete) {
    this.percentComplete = percentComplete;
  }

  public double getRecordsPerSecond() {
    return recordsPerSecond;
  }

  public void setRecordsPerSecond(double recordsPerSecond) {
    this.recordsPerSecond = recordsPerSecond;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(long startedAt) {
    this.startedAt = startedAt;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }
}
