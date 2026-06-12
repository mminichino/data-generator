package com.codelry.util.generator.controller;

import com.codelry.util.generator.dto.ConnectionParameters;
import com.codelry.util.generator.dto.CouchbaseConnectionConfig;
import com.codelry.util.generator.service.CouchbaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/database/connect/couchbase")
public class CouchbaseConnectionController {

  private static final Logger logger = LoggerFactory.getLogger(CouchbaseConnectionController.class);
  private final CouchbaseConnectionManager couchbaseConnectionManager;

  public CouchbaseConnectionController(CouchbaseConnectionManager couchbaseConnectionManager) {
    this.couchbaseConnectionManager = couchbaseConnectionManager;
  }

  @PostMapping("/connect")
  public ResponseEntity<?> connect(@RequestHeader(value = "X-User-Id") String userId,
                                   @RequestBody ConnectionParameters parameters) {
    try {
      CouchbaseConnectionConfig config = new CouchbaseConnectionConfig();
      config.setHost(parameters.getHost());
      config.setUsername(parameters.getUsername());
      config.setPassword(parameters.getPassword());
      config.setUseTls(parameters.isUseSsl());
      config.setTlsSkipVerify(parameters.isTlsSkipVerify());
      if (StringUtils.hasText(parameters.getDatabase())) {
        config.setBucket(parameters.getDatabase());
      }
      couchbaseConnectionManager.connect(userId, config);
      return ResponseEntity.ok(Map.of(
          "status", "connected",
          "message", "Successfully connected to Couchbase"
      ));
    } catch (Exception e) {
      logger.error("Failed to connect to Couchbase", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", "Failed to connect: " + e.getMessage()
      ));
    }
  }

  @PostMapping("/disconnect")
  public ResponseEntity<?> disconnect(@RequestHeader(value = "X-User-Id") String userId) {
    try {
      if (couchbaseConnectionManager.isNotConnected(userId)) {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", "This user is not connected to Couchbase"
        ));
      }
      couchbaseConnectionManager.disconnect(userId);
      return ResponseEntity.ok(Map.of(
          "status", "disconnected",
          "message", "Successfully disconnected from Couchbase"
      ));
    } catch (Exception e) {
      logger.error("Failed to disconnect from Couchbase", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", "Failed to disconnect: " + e.getMessage()
      ));
    }
  }

  @GetMapping("/status")
  public ResponseEntity<?> getStatus(@RequestHeader(value = "X-User-Id") String userId) {
    boolean connected = !couchbaseConnectionManager.isNotConnected(userId);
    return ResponseEntity.ok(Map.of(
        "connected", connected
    ));
  }
}
