package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EntityCollection {

  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("nosql")
  public boolean nosql;

  @JsonProperty("tables")
  public List<EntityDefinition> entities;

  public EntityCollection() {}

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public boolean isNosql() { return nosql; }
  public void setNosql(boolean nosql) { this.nosql = nosql; }

  public List<EntityDefinition> getEntities() { return entities; }
  public void setEntities(List<EntityDefinition> entities) { this.entities = entities; }
}
