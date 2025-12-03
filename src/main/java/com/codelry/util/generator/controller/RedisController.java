package com.codelry.util.generator.controller;

import com.codelry.util.generator.driver.Redis;
import com.codelry.util.generator.dto.*;
import com.codelry.util.generator.service.ReactiveRedisJsonTemplate;
import com.codelry.util.generator.service.RedisConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RestController
@RequestMapping("/api/generate")
public class RedisController {

  private static final Logger logger = LoggerFactory.getLogger(RedisController.class);
  private final RedisConnectionManager redisConnectionManager;

  public RedisController(RedisConnectionManager redisConnectionManager) {
    this.redisConnectionManager = redisConnectionManager;
  }

  @PostMapping("/redis")
  public ResponseEntity<GenerateResponse> generate(@RequestBody EntityCollection schema) {
    try {
      logger.info("Generating Redis data for schema collection {} (tables: {}) nosql={}",
          schema.getName(), schema.getEntities() != null ? schema.getEntities().size() : 0, schema.isNosql());

      ReactiveRedisTemplate<String, String> reactiveTemplate = redisConnectionManager.reactiveRedisTemplate();
      ReactiveRedisJsonTemplate<String, String> reactiveJsonTemplate = redisConnectionManager.reactiveRedisJsonTemplate();
      Redis driver = new Redis();
      driver.init(schema, 1);
      driver.connect(reactiveTemplate, reactiveJsonTemplate);
      if (redisConnectionManager.isUseJson()) {
        driver.setUseJson(true);
      }
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
