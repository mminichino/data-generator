package com.codelry.util.generator.controller;
import com.codelry.util.generator.dto.ConnectionParameters;
import com.codelry.util.generator.dto.RedisConnectionConfig;
import com.codelry.util.generator.service.RedisConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/database/connect/redis")
public class RedisConnectionController {

  private static final Logger logger = LoggerFactory.getLogger(RedisConnectionController.class);
  private final RedisConnectionManager redisConnectionManager;

  public RedisConnectionController(RedisConnectionManager redisConnectionManager) {
    this.redisConnectionManager = redisConnectionManager;
  }

  @PostMapping("/connect")
  public ResponseEntity<?> connect(@RequestHeader(value = "X-User-Id") String userId,
                                   @RequestBody ConnectionParameters parameters) {
    try {
      RedisConnectionConfig config = new RedisConnectionConfig();
      config.setHost(parameters.getHost());
      config.setPort(parameters.getPort());
      config.setPassword(parameters.getPassword());
      config.setUseJson(parameters.isUseJson());
      redisConnectionManager.connect(userId, config);
      return ResponseEntity.ok(Map.of(
          "status", "connected",
          "message", "Successfully connected to Redis"
      ));
    } catch (Exception e) {
      logger.error("Failed to connect", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", "Failed to connect: " + e.getMessage()
      ));
    }
  }

  @PostMapping("/disconnect")
  public ResponseEntity<?> disconnect(@RequestHeader(value = "X-User-Id") String userId) {
    try {
      if (redisConnectionManager.isNotConnected(userId)) {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", "This user is not connected to Redis"
        ));
      }
      redisConnectionManager.disconnect(userId);
      return ResponseEntity.ok(Map.of(
          "status", "disconnected",
          "message", "Successfully disconnected from Redis"
      ));
    } catch (Exception e) {
      logger.error("Failed to disconnect from Redis", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", "Failed to disconnect: " + e.getMessage()
      ));
    }
  }

  @GetMapping("/status")
  public ResponseEntity<?> getStatus(@RequestHeader(value = "X-User-Id") String userId) {
    boolean connected = !redisConnectionManager.isNotConnected(userId);
    return ResponseEntity.ok(Map.of(
        "connected", connected
    ));
  }
}
