package com.codelry.util.generator.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Schema {
  private static final Logger LOGGER = LogManager.getLogger(Schema.class);
  private long index_start = 0;
  private long index_end = 1000;
  List<JsonNode> schema;

  public Schema(String name, long start, long end) {
    index_start = start;
    index_end = end;
    init(name);
  }

  public Schema(String name, long count) {
    index_start = 1;
    index_end = count;
    init(name);
  }

  public void init(String name) {
    URL file = Schema.class.getClassLoader().getResource(String.format("schema/%s.json", name));
    try (InputStream in = Objects.requireNonNull(file).openStream()) {
      ObjectMapper mapper = new ObjectMapper();
      schema = mapper.readValue(in, new TypeReference<List<JsonNode>>(){});
      for (JsonNode jsonNode : schema) {
        LOGGER.debug("Schema:\n{}", jsonNode);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to load schema " + name, e);
    }
  }

  public List<String> getKeyspaceList() {
    List<String> keyspaceList = new ArrayList<>();
    for (JsonNode schemaNode : schema) {
      keyspaceList.add(schemaNode.get("keyspace").asText());
    }
    return keyspaceList;
  }

  public List<String> jsonNodeToList(JsonNode json) {
    List<String> result = new ArrayList<>();
    if (json.isArray()) {
      for (JsonNode jsonNode : json) {
        result.add(jsonNode.asText());
      }
    }
    return result;
  }

  public List<Keyspace> getSchemaList() {
    List<Keyspace> keyspaceList = new ArrayList<>();
    for (JsonNode schemaNode : schema) {
      Keyspace keyspace = new Keyspace(
          schemaNode.get("keyspace").asText(),
          schemaNode.get("schema"),
          schemaNode.get("documentId").asText(),
          schemaNode.get("primary_index").asBoolean(),
          jsonNodeToList(schemaNode.get("secondary_index")));
      keyspaceList.add(keyspace);
    }
    return keyspaceList;
  }
}
