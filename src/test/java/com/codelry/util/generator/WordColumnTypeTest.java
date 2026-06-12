package com.codelry.util.generator;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.dto.EntityCollection;
import com.codelry.util.generator.dto.Field;
import com.codelry.util.generator.generator.FieldGenerator;
import com.codelry.util.generator.util.ColumnOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WordColumnTypeTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void getWordValueReadsOption() {
    assertEquals("hello", ColumnOptions.getWordValue(Map.of("value", "hello")));
  }

  @Test
  void generateWordColumnFromSchemaJson() throws Exception {
    String json = """
        {
          "id": "89dbb9fe-92c1-40fb-9373-1fc24d0a1e2d",
          "name": "statuses",
          "nosql": true,
          "tables": [{
            "id": "b4910a2f-8c62-440c-861b-da11e44d9304",
            "name": "records",
            "columns": [{
              "id": "e78a92cc-d3c1-47d6-9fea-753264e5e613",
              "name": "status",
              "type": "word",
              "nullable": false,
              "options": { "value": "active" }
            }]
          }]
        }
        """;

    EntityCollection schema = MAPPER.readValue(json, EntityCollection.class);
    FieldGenerator generator = new FieldGenerator();
    generator.init();

    Field field = generator.generate(schema.getEntities().get(0).getFields().get(0), 1);
    assertEquals("active", field.getValue());
  }

  @Test
  void serializeWordColumnAsJsonString() throws Exception {
    Entity entity = new Entity();
    entity.addField(new Field(null, "status", "active"));

    JsonNode json = entity.asJson();
    assertNotNull(json.get("status"));
    assertEquals("\"active\"", json.get("status").toString());
  }
}
