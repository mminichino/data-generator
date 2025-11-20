package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Schema {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("count")
    public int count;

    @JsonProperty("columns")
    public List<Column> columns;

    public Schema() {
    }

    public Schema(String id, String name, int count, List<Column> columns) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.columns = columns;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
