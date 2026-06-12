package com.codelry.util.generator.db;

public class AirlineRecord {
  public String code;
  public String name;

  public AirlineRecord(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return code + " " + name;
  }
}
