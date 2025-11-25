package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {
  private static final Logger logger = LoggerFactory.getLogger(Generator.class);
  private String id;
  private JsonNode document;
  private final Table table;

  public Generator(Table table) {
    this.table = table;
  }

  public Record generate() {
    document = processMain();
    id = processId();
    return new Record(getId(), getDocument());
  }

  public JsonNode processMain() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addSerializer(Table.class, new GeneratorSerializer());
      mapper.registerModule(module);
      String template = mapper.writeValueAsString(table);
      logger.debug("Generating table {} index {}", table.getName(), table.getIndex().get());
      return mapper.readTree(template);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public String processId() {
    return UUID.randomUUID().toString();
  }

  public JsonNode getDocument() {
    return document;
  }

  public String getId() {
    return id;
  }
}
