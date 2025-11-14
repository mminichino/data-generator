package com.codelry.util.generator.db;

public class AreaCodeRecord {
  public String code;
  public String state;

  public AreaCodeRecord(String code, String state) {
    this.code = code;
    this.state = state;
  }

  public String getCode() {
    return code;
  }

  public String getState() {
    return state;
  }
}
