package com.codelry.util.generator.controller;

import com.codelry.util.generator.driver.JsonData;
import com.codelry.util.generator.driver.Redis;
import com.codelry.util.generator.dto.*;
import com.codelry.util.generator.service.ReactiveRedisJsonTemplate;
import com.codelry.util.generator.service.RedisConnectionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RestController
@RequestMapping("/api")
public class GenerateController {

  private static final Logger logger = LoggerFactory.getLogger(GenerateController.class);
  private final RedisConnectionManager redisConnectionManager;

  public GenerateController(RedisConnectionManager redisConnectionManager) {
    this.redisConnectionManager = redisConnectionManager;
  }

  @PostMapping("/generate")
  public ResponseEntity<GenerateResponse> generate(
      @RequestBody SchemaCollection schema,
      @RequestParam(value = "sample", required = false, defaultValue = "false") boolean sample) {
    try {
      logger.info("Generating data for schema collection {} (tables: {}) sample={} nosql={}"
          , schema.getName(), schema.getTables() != null ? schema.getTables().size() : 0, sample, schema.isNosql());

      List<JsonNode> samples = new ArrayList<>();
      if (sample) {
        JsonData driver = new JsonData();
        driver.init(schema, 1, 10);
        driver.generate();
        samples = driver.getRecords();
        logger.info("Samples generated successfully: {} records", samples.size());
      } else {
        logger.info("Inserting generated data using Redis driver");
        ReactiveRedisTemplate<String, String> reactiveTemplate = redisConnectionManager.reactiveRedisTemplate();
        ReactiveRedisJsonTemplate<String, String> reactiveJsonTemplate = redisConnectionManager.reactiveRedisJsonTemplate();
        Redis driver = new Redis();
        driver.init(schema, 1, 10);
        driver.connect(reactiveTemplate, reactiveJsonTemplate);
        if (redisConnectionManager.isUseJson()) {
          driver.setUseJson(true);
        }
        driver.generate();
      }

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
