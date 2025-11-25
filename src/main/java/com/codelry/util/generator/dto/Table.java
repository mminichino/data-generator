package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Table {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("count")
    public Integer count;

    @JsonProperty("keyFormat")
    public String keyFormat;

    @JsonProperty("columns")
    public List<Column> columns;

    public AtomicLong index = new AtomicLong(1);

    public Table() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public String getKeyFormat() { return keyFormat; }
    public void setKeyFormat(String keyFormat) { this.keyFormat = keyFormat; }

    public List<Column> getColumns() { return columns; }
    public void setColumns(List<Column> columns) { this.columns = columns; }

    public AtomicLong getIndex() { return index; }
    public void setIndex(AtomicLong index) { this.index = index; }
}
