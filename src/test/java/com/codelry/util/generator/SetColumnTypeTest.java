package com.codelry.util.generator;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.dto.EntityCollection;
import com.codelry.util.generator.dto.Field;
import com.codelry.util.generator.generator.FieldGenerator;
import com.codelry.util.generator.util.ColumnOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SetColumnTypeTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void getSetMembersReadsJsonList() {
    List<String> members = ColumnOptions.getSetMembers(Map.of("members", List.of("ATP")));
    assertEquals(List.of("ATP"), members);
  }

  @Test
  void generateSetColumnFromSchemaJson() throws Exception {
    String json = """
        {
          "id": "89dbb9fe-92c1-40fb-9373-1fc24d0a1e2d",
          "name": "fares",
          "nosql": true,
          "tables": [{
            "id": "b4910a2f-8c62-440c-861b-da11e44d9304",
            "name": "current_fares",
            "keyFormat": "{{ __table__ }}:{{ __uuid__ }}",
            "columns": [{
              "id": "e78a92cc-d3c1-47d6-9fea-753264e5e613",
              "name": "source",
              "type": "set",
              "nullable": false,
              "options": { "members": ["ATP"] }
            }]
          }]
        }
        """;

    EntityCollection schema = MAPPER.readValue(json, EntityCollection.class);
    FieldGenerator generator = new FieldGenerator();
    generator.init();

    Field field = generator.generate(schema.getEntities().get(0).getFields().get(0), 1);
    assertEquals(List.of("ATP"), field.getValue());
  }

  @Test
  void serializeSetColumnAsJsonArray() throws Exception {
    Entity entity = new Entity();
    entity.addField(new Field(null, "source", List.of("ATP")));

    JsonNode json = entity.asJson();
    assertNotNull(json.get("source"));
    assertEquals("[\"ATP\"]", json.get("source").toString());
  }
}
