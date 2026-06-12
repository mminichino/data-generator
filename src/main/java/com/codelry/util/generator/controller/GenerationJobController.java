package com.codelry.util.generator.controller;

import com.codelry.util.generator.dto.GenerationJobStatus;
import com.codelry.util.generator.service.GenerationJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generate/jobs")
public class GenerationJobController {

  private final GenerationJobService generationJobService;

  public GenerationJobController(GenerationJobService generationJobService) {
    this.generationJobService = generationJobService;
  }

  @GetMapping("/{jobId}")
  public ResponseEntity<GenerationJobStatus> getStatus(
      @RequestHeader(value = "X-User-Id") String userId,
      @PathVariable String jobId) {
    return ResponseEntity.ok(generationJobService.getJobStatus(userId, jobId));
  }

  @PostMapping("/{jobId}/cancel")
  public ResponseEntity<GenerationJobStatus> cancel(
      @RequestHeader(value = "X-User-Id") String userId,
      @PathVariable String jobId) {
    return ResponseEntity.ok(generationJobService.cancelJob(userId, jobId));
  }
}
