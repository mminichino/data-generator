package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SchemaCollection {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("nosql")
    public boolean nosql;

    @JsonProperty("tables")
    public List<Table> tables;

    public SchemaCollection() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isNosql() { return nosql; }
    public void setNosql(boolean nosql) { this.nosql = nosql; }

    public List<Table> getTables() { return tables; }
    public void setTables(List<Table> tables) { this.tables = tables; }
}
