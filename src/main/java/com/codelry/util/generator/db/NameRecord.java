package com.codelry.util.generator.db;

public class NameRecord {
  public String first;
  public String last;
  public String gender;

  public NameRecord(String first, String last, String gender) {
    this.first = first;
    this.last = last;
    this.gender = gender;
  }

  public String getFirst() {
    return first;
  }

  public String getLast() {
    return last;
  }

  public String fullName() {
    return first + " " + last;
  }

  public String emailAddress() {
    return first.toLowerCase() + "." + last.toLowerCase() + "@example.com";
  }

  public String getGender() {
    return gender;
  }
}
