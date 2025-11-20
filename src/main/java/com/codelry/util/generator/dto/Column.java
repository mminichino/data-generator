package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Column {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("type")
    public ColumnType type;

    @JsonProperty("nullable")
    public boolean nullable;

    @JsonProperty("primaryKey")
    public boolean primaryKey;

    @JsonProperty("options")
    public Map<String, Object> options;

    public Column() {
    }

    public Column(String id, String name, ColumnType type, boolean nullable, Map<String, Object> options) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
