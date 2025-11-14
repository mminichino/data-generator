package com.codelry.util.generator.generator;

import com.fasterxml.jackson.databind.JsonNode;

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
}
