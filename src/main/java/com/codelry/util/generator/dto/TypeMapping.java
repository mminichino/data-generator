package com.codelry.util.generator.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;

public enum TypeMapping {
  STRING("String", String.class),
  LONG("Long", Long.class),
  INTEGER("Integer", Integer.class),
  DOUBLE("Double", Double.class),
  FLOAT("Float", Float.class),
  BOOLEAN("Boolean", Boolean.class),
  LOCAL_DATE_TIME("LocalDateTime", LocalDateTime.class),
  LOCAL_DATE("LocalDate", LocalDate.class);

  private final String dataType;
  private final Class<?> dataClass;

  TypeMapping(String dataType, Class<?> dataClass) {
    this.dataType = dataType;
    this.dataClass = dataClass;
  }

  public String getDataType() {
    return dataType;
  }

  public Class<?> getDataClass() {
    return dataClass;
  }

  public static TypeMapping fromString(String text) {
    for (TypeMapping b : TypeMapping.values()) {
      if (b.dataType.equalsIgnoreCase(text)) {
        return b;
      }
    }
    throw new IllegalArgumentException("No enum constant with data type " + text);
  }

  public static Class<?> getClassFromString(String text) {
    TypeMapping enumValue = fromString(text);
    return enumValue.getDataClass();
  }
}
