package com.codelry.util.generator.controller;

import com.codelry.util.generator.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  @PostMapping("/generate")
  public ResponseEntity<GenerateResponse> generate(
      @RequestBody SchemaCollection request,
      @RequestParam(value = "sample", required = false, defaultValue = "false") boolean sample) {
    try {
      logger.info("Generating data for schema collection {} (tables: {}) sample={} nosql={}"
          , request.getName(), request.getTables() != null ? request.getTables().size() : 0, sample, request.isNosql());

      ObjectMapper objectMapper = new ObjectMapper();
      String result = objectMapper.writeValueAsString(request);

      List<Map<String, Object>> samples = null;
      if (sample) {
        if (request.getTables() != null && !request.getTables().isEmpty()) {
          Table table = request.getTables().get(0);
          samples = buildSamples(table);
        } else {
          samples = Collections.emptyList();
        }
      }

      GenerateResponse response = new GenerateResponse("success", result, samples);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      GenerateResponse errorResponse = new GenerateResponse("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  private List<Map<String, Object>> buildSamples(Table table) {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      Map<String, Object> row = new LinkedHashMap<>();
      if (table.getColumns() != null) {
        for (Column col : table.getColumns()) {
          // simple stubbed values by type
          Object value;
          switch (col.getType()) {
            case SEQUENTIAL_NUMBER:
            case NUMBER:
              value = i;
              break;
            case BOOLEAN:
              value = (i % 2 == 0);
              break;
            case UUID:
              value = java.util.UUID.randomUUID().toString();
              break;
            case FIRST_NAME:
              value = "John";
              break;
            case LAST_NAME:
              value = "Doe";
              break;
            case FULL_NAME:
              value = "John Doe";
              break;
            case EMAIL:
              value = "user" + i + "@example.com";
              break;
            case PHONE_NUMBER:
              value = "555-010" + i;
              break;
            case STREET_ADDRESS:
              value = i + " Main St";
              break;
            case CITY:
              value = "Metropolis";
              break;
            case STATE:
              value = "CA";
              break;
            case ZIPCODE:
              value = "9000" + i;
              break;
            case CREDIT_CARD:
              value = "4111-1111-1111-111" + (i % 10);
              break;
            case ACCOUNT_NUMBER:
              value = "ACCT" + i;
              break;
            case DOLLAR_AMOUNT:
              value = i * 10.5;
              break;
            case PRODUCT_NAME:
              value = "Widget " + i;
              break;
            case PRODUCT_TYPE:
              value = "Type " + i;
              break;
            case MANUFACTURER:
              value = "Acme";
              break;
            case DATE:
              value = new java.util.Date().toString();
              break;
            case TIMESTAMP:
              value = System.currentTimeMillis();
              break;
            case IP_ADDRESS:
              value = "192.168.0." + i;
              break;
            case MAC_ADDRESS:
              value = String.format("00:11:22:33:44:%02X", i);
              break;
            case SET:
              value = "A";
              break;
            case TEXT:
            default:
              value = col.getName() + "_" + i;
          }
          row.put(col.getName(), value);
        }
      }
      rows.add(row);
    }
    return rows;
  }
}
