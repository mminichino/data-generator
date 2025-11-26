package com.codelry.util.generator.generator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class Record {
  public String documentId;
  public JsonNode document;

  public Record(String id, JsonNode document) {
    this.documentId = id;
    this.document = document;
  }

  public String getId() {
    return documentId;
  }

  public JsonNode getDocument() {
    return document;
  }

  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();
    document.properties().forEach(entry -> map.put(entry.getKey(), entry.getValue().asText()));
    return map;
  }
}
