package com.codelry.util.generator.dto;

import com.codelry.util.generator.db.DatabaseManager;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class FieldDefinition {

  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  private String name;

  private TypeMapping dataType;

  @JsonProperty("type")
  private ColumnType type;

  private String columnName;

  @JsonProperty("primaryKey")
  private boolean primaryKey;

  private boolean generated;

  @JsonProperty("nullable")
  private boolean nullable = true;

  private Integer length;

  @JsonProperty("options")
  public Map<String, Object> options;

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

  public void setDataTypeFromType() {
    switch (type) {
      case SEQUENTIAL_NUMBER:
        setDataType(TypeMapping.LONG);
        break;
      case BOOLEAN:
        setDataType(TypeMapping.BOOLEAN);
        break;
      case NUMBER:
        boolean isDecimal = this.options.containsKey("isDecimal") && Boolean.parseBoolean(this.options.get("isDecimal").toString());
        if (isDecimal) {
          setDataType(TypeMapping.DOUBLE);
        } else {
          setDataType(TypeMapping.LONG);
        }
        break;
      case DOLLAR_AMOUNT:
        setDataType(TypeMapping.FLOAT);
        break;
      case DATE, TIMESTAMP:
        setDataType(TypeMapping.LOCAL_DATE_TIME);
        break;
      default:
        setDataType(TypeMapping.STRING);
        break;
    }
  }

  public void setDataLengthFromType() {
    DatabaseManager databaseManager = DatabaseManager.getInstance();
    switch (type) {
      case FIRST_NAME:
        setLength(databaseManager.getNameFieldLength("first"));
        break;
      case LAST_NAME:
        setLength(databaseManager.getNameFieldLength("last"));
        break;
      case FULL_NAME:
        setLength(databaseManager.getNameFieldLength("fullName"));
        break;
      case EMAIL:
        setLength(databaseManager.getNameFieldLength("emailAddress"));
        break;
      case STREET_ADDRESS:
        setLength(databaseManager.getAddressFieldLength("streetAddress"));
        break;
      case CITY:
        setLength(databaseManager.getAddressFieldLength("city"));
        break;
      case STATE:
        setLength(databaseManager.getAddressFieldLength("state"));
        break;
      case ZIPCODE:
        setLength(databaseManager.getAddressFieldLength("zip"));
    }
  }

  public TypeMapping getDataType() {
    return dataType;
  }

  public void setDataType(TypeMapping dataType) {
    this.dataType = dataType;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
  }

  public boolean isGenerated() {
    return generated;
  }

  public void setGenerated(boolean generated) {
    this.generated = generated;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public void setOptions(Map<String, Object> options) {
    this.options = options;
  }
}
