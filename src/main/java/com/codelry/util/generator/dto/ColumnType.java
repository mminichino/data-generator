package com.codelry.util.generator.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum ColumnType {
  @JsonProperty("sequentialNumber") SEQUENTIAL_NUMBER,
  @JsonProperty("text") TEXT,
  @JsonProperty("number") NUMBER,
  @JsonProperty("boolean") BOOLEAN,
  @JsonProperty("uuid") UUID,
  @JsonProperty("firstName") FIRST_NAME,
  @JsonProperty("lastName") LAST_NAME,
  @JsonProperty("fullName") FULL_NAME,
  @JsonProperty("email") EMAIL,
  @JsonProperty("phoneNumber") PHONE_NUMBER,
  @JsonProperty("streetAddress") STREET_ADDRESS,
  @JsonProperty("city") CITY,
  @JsonProperty("state") STATE,
  @JsonProperty("zipcode") ZIPCODE,
  @JsonProperty("creditCard") CREDIT_CARD,
  @JsonProperty("accountNumber") ACCOUNT_NUMBER,
  @JsonProperty("dollarAmount") DOLLAR_AMOUNT,
  @JsonProperty("productName") PRODUCT_NAME,
  @JsonProperty("productType") PRODUCT_TYPE,
  @JsonProperty("manufacturer") MANUFACTURER,
  @JsonProperty("date") DATE,
  @JsonProperty("timestamp") TIMESTAMP,
  @JsonProperty("ipAddress") IP_ADDRESS,
  @JsonProperty("macAddress") MAC_ADDRESS,
  @JsonProperty("set") SET,
  @JsonEnumDefaultValue UNKNOWN
}
