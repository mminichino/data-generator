package com.codelry.util.generator;

import com.codelry.util.generator.controller.GenerateController;
import com.codelry.util.generator.dto.*;
import com.codelry.util.generator.generator.GeneratorSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityGenerationTest {
  private static final Logger logger = LoggerFactory.getLogger(EntityGenerationTest.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static EntityCollection schema;

  @BeforeAll
  public static void setUp() throws Exception {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();

    try {
      schema = mapper.readValue(loader.getResourceAsStream("schema.json"), EntityCollection.class);
      for (EntityDefinition entity : schema.getEntities()) {
        for (FieldDefinition field : entity.getFields()) {
          field.setDataTypeFromType();
        }
      }
    } catch (IOException e) {
      System.out.println("can not open template file: " + e.getMessage());
      e.printStackTrace(System.err);
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testSchemaCollection() {
    for (EntityDefinition entity : schema.getEntities()) {
      for (FieldDefinition field : entity.getFields()) {
        logger.info("{} {}", field.getName(), field.getType());
      }
    }
  }

  @Test
  public void testSchemaSerialization() {
    for (EntityDefinition entity : schema.getEntities()) {
      for (FieldDefinition field : entity.getFields()) {
        logger.info("{}", field.getDataType());
      }
    }
  }
}
