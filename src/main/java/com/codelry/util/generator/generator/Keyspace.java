package com.codelry.util.generator.generator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Keyspace {
  public String bucket;
  public String scope;
  public String collection;
  public JsonNode template;
  public String idTemplate;
  public boolean primaryIndex;
  public List<String> secondaryIndexFields = new ArrayList<>();

  public Keyspace(String keyspace, JsonNode schema, String id, boolean primary, List<String> indexFields) {
    List<String> keys = Arrays.asList(keyspace.split("\\."));
    if (keys.size() != 3) {
      throw new RuntimeException("Invalid keyspace: " + keyspace);
    }
    bucket = keys.get(0);
    scope = keys.get(1);
    collection = keys.get(2);
    template = schema;
    idTemplate = id;
    primaryIndex = primary;
    secondaryIndexFields.addAll(indexFields);
  }

  public String toString() {
    return bucket + "." + scope + "." + collection;
  }

  public String getBucket() {
    return bucket;
  }

  public String getScope() {
    return scope;
  }

  public String getCollection() {
    return collection;
  }

  public JsonNode getTemplate() {
    return template;
  }
}
