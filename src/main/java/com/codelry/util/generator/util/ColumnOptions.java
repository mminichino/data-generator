package com.codelry.util.generator.util;

import java.util.List;
import java.util.Map;

public final class ColumnOptions {

  private static final List<String> DEFAULT_SET_MEMBERS = List.of("one", "two", "three");
  private static final String DEFAULT_WORD = "";

  private ColumnOptions() {}

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
