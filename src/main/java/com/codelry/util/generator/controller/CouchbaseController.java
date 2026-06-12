package com.codelry.util.generator.controller;

import com.codelry.util.generator.dto.EntityCollection;
import com.codelry.util.generator.dto.StartGenerationResponse;
import com.codelry.util.generator.service.GenerationJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generate")
public class CouchbaseController {

  private static final Logger logger = LoggerFactory.getLogger(CouchbaseController.class);
  private final GenerationJobService generationJobService;

  public CouchbaseController(GenerationJobService generationJobService) {
    this.generationJobService = generationJobService;
  }

  @PostMapping("/couchbase")
  public ResponseEntity<StartGenerationResponse> generate(
      @RequestHeader(value = "X-User-Id") String userId,
      @RequestBody EntityCollection schema) {
    logger.info("Starting Couchbase generation for schema collection {} (tables: {}) nosql={}",
        schema.getName(), schema.getEntities() != null ? schema.getEntities().size() : 0, schema.isNosql());
    StartGenerationResponse response = generationJobService.startCouchbaseJob(userId, schema);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
