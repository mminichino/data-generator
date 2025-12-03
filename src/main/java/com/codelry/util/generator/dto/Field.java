package com.codelry.util.generator.dto;

public class Field {
  public TypeMapping dataType;
  public String name;
  public String comment;
  public Object value;

  public Field(TypeMapping dataType, String name, String comment, Object value) {
    this.dataType = dataType;
    this.name = name;
    this.comment = comment;
    this.value = value;
  }

  public Field(TypeMapping dataType, String name, Object value) {
    this.dataType = dataType;
    this.name = name;
    this.value = value;
  }

  public Field() {}

  public TypeMapping getDataType() {
    return dataType;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public Object getValue() {
    return value;
  }

  public void setDataType(TypeMapping dataType) {
    this.dataType = dataType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}
