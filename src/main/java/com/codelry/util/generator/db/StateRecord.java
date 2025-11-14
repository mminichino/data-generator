package com.codelry.util.generator.db;

public class StateRecord {
  public String city;
  public String state;
  public String zip;
  public String plusfour;

  public StateRecord(String city, String state, String zip, String plusfour) {
    this.city = city;
    this.state = state;
    this.zip = zip;
    this.plusfour = plusfour;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getZip() {
    return zip;
  }

  public String getPlusFour() {
    return plusfour;
  }
}
