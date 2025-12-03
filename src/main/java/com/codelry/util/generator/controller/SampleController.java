package com.codelry.util.generator.controller;

import com.codelry.util.generator.driver.JsonData;
import com.codelry.util.generator.dto.EntityCollection;
import com.codelry.util.generator.dto.GenerateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
public class SampleController {
  private static final Logger logger = LoggerFactory.getLogger(SampleController.class);

  public SampleController() {}

  @PostMapping("/samples")
  public ResponseEntity<GenerateResponse> generate(@RequestBody EntityCollection schema) {
    try {
      logger.info("Generating data for schema collection {} (tables: {}) nosql={}",
          schema.getName(), schema.getEntities() != null ? schema.getEntities().size() : 0, schema.isNosql());

      List<JsonNode> samples;
      JsonData driver = new JsonData();
      driver.init(schema, 1, 10);
      driver.generate();
      samples = driver.getRecords();
      logger.info("Samples generated successfully: {} records", samples.size());

      String result = new ObjectMapper().writeValueAsString(schema);

      GenerateResponse response = new GenerateResponse("success", result, samples);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      GenerateResponse errorResponse = new GenerateResponse("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
}
