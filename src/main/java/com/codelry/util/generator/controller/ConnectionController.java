package com.codelry.util.generator.controller;

import com.codelry.util.generator.config.AppConfig;
import com.codelry.util.generator.dto.ConnectionParameters;
import com.codelry.util.generator.dto.RedisConnectionConfig;
import com.codelry.util.generator.service.RedisConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/database")
public class ConnectionController {

  @Autowired
  private AppConfig appConfig;

  private static final Logger logger = LoggerFactory.getLogger(ConnectionController.class);
  private final RedisConnectionManager redisConnectionManager;

  public ConnectionController(RedisConnectionManager redisConnectionManager) {
    this.redisConnectionManager = redisConnectionManager;
  }

  @PostMapping("/connect")
  public ResponseEntity<?> connect(@RequestBody ConnectionParameters parameters) {
    try {
      if (Objects.equals(parameters.getType(), "redis")) {
        RedisConnectionConfig config = new RedisConnectionConfig();
        config.setHost(parameters.getHost());
        config.setPort(parameters.getPort());
        config.setPassword(parameters.getPassword());
        config.setUseJson(parameters.isUseJson());
        redisConnectionManager.connect(config);
        appConfig.setConnected(true);
        appConfig.setType("redis");
        return ResponseEntity.ok(Map.of(
            "status", "connected",
            "message", "Successfully connected to Redis"
        ));
      } else {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", "Unsupported connection type: " + parameters.getType()
        ));
      }
    } catch (Exception e) {
      logger.error("Failed to connect", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", "Failed to connect: " + e.getMessage()
      ));
    }
  }

  @PostMapping("/disconnect")
  public ResponseEntity<?> disconnect() {
    try {
      if (appConfig.isConnected()) {
        if (Objects.equals(appConfig.getType(), "redis")) {
          redisConnectionManager.disconnect();
          return ResponseEntity.ok(Map.of(
              "status", "disconnected",
              "message", "Successfully disconnected from Redis"
          ));
        } else {
          return ResponseEntity.badRequest().body(Map.of(
              "status", "error",
              "message", "Unsupported connection type: " + appConfig.getType()
          ));
        }
      } else {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", "Database is not connected"
        ));
      }
    } catch (Exception e) {
      logger.error("Failed to disconnect from Redis", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", "Failed to disconnect: " + e.getMessage()
      ));
    }
  }

  @GetMapping("/status")
  public ResponseEntity<?> getStatus() {
    return ResponseEntity.ok(Map.of(
        "connected", appConfig.isConnected()
    ));
  }
}
