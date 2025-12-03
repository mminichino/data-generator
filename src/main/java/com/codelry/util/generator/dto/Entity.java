package com.codelry.util.generator.dto;

import com.codelry.util.generator.generator.EntitySerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.ArrayList;
import java.util.List;

public class Entity {
  public String id;
  public List<Field> fields = new ArrayList<>();

  public Entity(String id, List<Field> fields) {
    this.id = id;
    this.fields = fields;
  }

  public Entity() {}

  public String getId() { return id; }

  public List<Field> getFields() { return fields; }

  public void setId(String id) { this.id = id; }

  public void setFields(List<Field> fields) { this.fields = fields; }

  public void addField(Field field) { fields.add(field); }

  public JsonNode asJson() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Entity.class, new EntitySerializer());
    mapper.registerModule(module);
    return mapper.valueToTree(this);
  }
}
