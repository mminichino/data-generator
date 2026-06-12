package com.codelry.util.generator.db;

public class AirportRecord {
  public String code;
  public String name;
  public String city;

  public AirportRecord(String code, String city, String name) {
    this.code = code;
    this.name = name;
    this.city = city;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getCity() {
    return city;
  }

  public String toString() {
    return code + " " + name + " " + city;
  }
}
