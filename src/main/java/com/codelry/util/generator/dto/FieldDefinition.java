package com.codelry.util.generator.dto;

import com.codelry.util.generator.db.DatabaseManager;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

public class FieldDefinition {

  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  private String name;

  private TypeMapping dataType;
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

  @JsonSetter("type")
  public void setFromType(String type) {
    this.dataType = TypeMapping.fromJsonProperty(type);
    this.type = ColumnType.fromText(type);
    setDataLengthFromType();
  }

  public void setDataLengthFromType() {
    DatabaseManager databaseManager = DatabaseManager.getInstance();
    switch (this.type) {
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
        break;
      case UUID:
        setLength(36);
        break;
      case CREDIT_CARD:
        setLength(16);
        break;
      case PHONE_NUMBER:
        setLength(10);
        break;
      case ACCOUNT_NUMBER:
        setLength(12);
        break;
      case TEXT, SET:
        setLength(255);
        break;
      case MAC_ADDRESS:
        setLength(17);
        break;
      case IP_ADDRESS:
        setLength(15);
        break;
      case PRODUCT_NAME:
        setLength(databaseManager.getProductFieldLength("name"));
        break;
      case PRODUCT_TYPE:
        setLength(databaseManager.getProductFieldLength("category"));
        break;
      case MANUFACTURER:
        setLength(databaseManager.getProductFieldLength("manufacturer"));
        break;
      default:
        break;
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
