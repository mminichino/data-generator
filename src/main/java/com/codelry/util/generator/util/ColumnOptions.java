package com.codelry.util.generator.util;

import java.util.List;
import java.util.Map;

public final class ColumnOptions {

  private static final List<String> DEFAULT_SET_MEMBERS = List.of("one", "two", "three");
  private static final String DEFAULT_WORD = "";
  private static final long DEFAULT_VALUE = 0L;

  private ColumnOptions() {}

  public static Number getNumericValue(Map<String, Object> options) {
    if (options == null || !options.containsKey("value")) {
      return DEFAULT_VALUE;
    }

    Object value = options.get("value");
    if (value instanceof Number number) {
      return number;
    }

    String text = String.valueOf(value).trim();
    if (text.isEmpty()) {
      return DEFAULT_VALUE;
    }

    try {
      return Long.parseLong(text);
    } catch (NumberFormatException ignored) {
      // not an integer, fall through to decimal parsing
    }

    try {
      return Double.parseDouble(text);
    } catch (NumberFormatException ignored) {
      return DEFAULT_VALUE;
    }
  }

  public static List<String> getSetMembers(Map<String, Object> options) {
    if (options == null || !options.containsKey("members")) {
      return DEFAULT_SET_MEMBERS;
    }

    Object members = options.get("members");
    if (members instanceof String[] array) {
      return List.of(array);
    }
    if (members instanceof List<?> list) {
      return list.stream().map(String::valueOf).toList();
    }
    return DEFAULT_SET_MEMBERS;
  }

  public static String getWordValue(Map<String, Object> options) {
    if (options == null || !options.containsKey("value")) {
      return DEFAULT_WORD;
    }
    return String.valueOf(options.get("value"));
  }
}
