package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EntityDefinition {

  @JsonProperty("id")
  public String id;

  private String entityName;

  @JsonProperty("name")
  private String tableName;

  @JsonProperty("count")
  public int count;

  @JsonProperty("keyFormat")
  public String keyFormat;

  @JsonProperty("columns")
  private List<FieldDefinition> fields;

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getKeyFormat() {
    return keyFormat;
  }

  public void setKeyFormat(String keyFormat) {
    this.keyFormat = keyFormat;
  }

  public List<FieldDefinition> getFields() {
    return fields;
  }

  public void setFields(List<FieldDefinition> fields) {
    this.fields = fields;
  }
}
