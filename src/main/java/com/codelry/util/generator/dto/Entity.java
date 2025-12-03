package com.codelry.util.generator.dto;

import com.codelry.util.generator.generator.EntitySerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {
  public String id;
  public long index;
  public List<Field> fields = new ArrayList<>();

  public Entity(String id, List<Field> fields) {
    this.id = id;
    this.fields = fields;
  }

  public Entity() {}

  public String getId() { return id; }

  public long getIndex() { return index; }

  public List<Field> getFields() { return fields; }

  public void setId(String id) { this.id = id; }

  public void setIndex(long index) { this.index = index; }

  public void setFields(List<Field> fields) { this.fields = fields; }

  public void addField(Field field) { fields.add(field); }

  public Map<String, String> asMap() {
    Map<String, String> map = new HashMap<>();
    getFields().forEach(field ->
        map.put(field.getName(), String.valueOf(field.getValue())));
    return map;
  }

  public JsonNode asJson() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Entity.class, new EntitySerializer());
    mapper.registerModule(module);
    return mapper.valueToTree(this);
  }
}
