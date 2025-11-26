package com.codelry.util.generator.service;

import io.lettuce.core.protocol.ProtocolKeyword;

public enum JsonCommand implements ProtocolKeyword {
  JSON_SET("JSON.SET"),
  JSON_GET("JSON.GET"),
  JSON_DEL("JSON.DEL"),
  JSON_TYPE("JSON.TYPE"),
  JSON_NUMINCRBY("JSON.NUMINCRBY"),
  JSON_NUMMULTBY("JSON.NUMMULTBY"),
  JSON_TOGGLE("JSON.TOGGLE"),
  JSON_STRAPPEND("JSON.STRAPPEND"),
  JSON_CLEAR("JSON.CLEAR"),
  JSON_ARRAPPEND("JSON.ARRAPPEND"),
  JSON_ARRLEN("JSON.ARRLEN"),
  JSON_ARRINDEX("JSON.ARRINDEX"),
  JSON_ARRPOP("JSON.ARRPOP"),
  JSON_ARRTRIM("JSON.ARRTRIM"),
  JSON_OBJKEYS("JSON.OBJKEYS"),
  JSON_OBJLEN("JSON.OBJLEN"),
  JSON_ARRINSERT("JSON.ARRINSERT"),
  JSON_STRLEN("JSON.STRLEN");

  private final byte[] bytes;
  private final String command;

  JsonCommand(String command) {
    this.command = command;
    this.bytes = command.getBytes();
  }

  @Override
  public byte[] getBytes() {
    return bytes;
  }

  @Override
  public String toString() {
    return command;
  }
}
