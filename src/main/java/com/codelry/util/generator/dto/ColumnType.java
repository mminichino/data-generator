package com.codelry.util.generator.dto;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public enum ColumnType {
  SEQUENTIAL_NUMBER("sequentialNumber"),
  TEXT("text"),
  NUMBER("number"),
  BOOLEAN("boolean"),
  UUID("uuid"),
  FIRST_NAME("firstName"),
  LAST_NAME("lastName"),
  FULL_NAME("fullName"),
  EMAIL("email"),
  PHONE_NUMBER("phoneNumber"),
  STREET_ADDRESS("streetAddress"),
  CITY("city"),
  STATE("state"),
  ZIPCODE("zipcode"),
  CREDIT_CARD("creditCard"),
  ACCOUNT_NUMBER("accountNumber"),
  DOLLAR_AMOUNT("dollarAmount"),
  PRODUCT_NAME("productName"),
  PRODUCT_TYPE("productType"),
  MANUFACTURER("manufacturer"),
  DATE("date"),
  TIMESTAMP("timestamp"),
  IP_ADDRESS("ipAddress"),
  MAC_ADDRESS("macAddress"),
  SET("set"),
  UNKNOWN("unknown");

  private final String text;

  private static final Map<String, ColumnType> BY_TEXT =
      Arrays.stream(values())
          .collect(Collectors.toMap(
              ct -> ct.text,
              ct -> ct
          ));

  ColumnType(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public static ColumnType fromText(String text) {
    if (text == null) {
      return UNKNOWN;
    }
    return BY_TEXT.getOrDefault(text, UNKNOWN);
  }

  public static ColumnType fromAny(String value) {
    if (value == null) {
      return UNKNOWN;
    }

    try {
      return ColumnType.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {}

    return fromText(value);
  }
}
