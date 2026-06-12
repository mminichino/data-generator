package com.codelry.util.generator.controller;

import com.codelry.util.generator.driver.Couchbase;
import com.codelry.util.generator.dto.EntityCollection;
import com.codelry.util.generator.dto.GenerateResponse;
import com.codelry.util.generator.service.CouchbaseConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generate")
public class CouchbaseController {

  private static final Logger logger = LoggerFactory.getLogger(CouchbaseController.class);
  private final CouchbaseConnectionManager couchbaseConnectionManager;

  public CouchbaseController(CouchbaseConnectionManager couchbaseConnectionManager) {
    this.couchbaseConnectionManager = couchbaseConnectionManager;
  }

  @PostMapping("/couchbase")
  public ResponseEntity<GenerateResponse> generate(
      @RequestHeader(value = "X-User-Id") String userId,
      @RequestBody EntityCollection schema) {
    try {
      logger.info("Generating Couchbase data for schema collection {} (tables: {}) nosql={}",
          schema.getName(), schema.getEntities() != null ? schema.getEntities().size() : 0, schema.isNosql());

      Couchbase driver = new Couchbase();
      driver.init(schema, 1);
      driver.connect(couchbaseConnectionManager.getCollection(userId));
      driver.generate();

      String result = new ObjectMapper().writeValueAsString(schema);
      GenerateResponse response = new GenerateResponse("success", result);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      GenerateResponse errorResponse = new GenerateResponse("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
}
