package com.codelry.util.generator.controller;

import com.codelry.util.generator.dto.GenerateRequest;
import com.codelry.util.generator.dto.GenerateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GenerateController {

  @PostMapping("/generate")
  public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest request) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String result = objectMapper.writeValueAsString(request);

      GenerateResponse response = new GenerateResponse("success", result);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      GenerateResponse errorResponse = new GenerateResponse("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  private String processRequest(GenerateRequest request) {
    return "Processed: " + request.getCount();
  }
}
