package com.codelry.util.generator.db;

public class AddressRecord {
  public String number;
  public String street;
  public String city;
  public String state;
  public String zip;

  public AddressRecord(String number, String street, String city, String state, String zip) {
    this.number = number;
    this.street = street;
    this.city = city;
    this.state = state;
    this.zip = zip;
  }

  public String getNumber() {
    return number;
  }

  public String getStreet() {
    return street;
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
}
