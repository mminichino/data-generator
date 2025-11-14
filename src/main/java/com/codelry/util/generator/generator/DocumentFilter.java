package com.codelry.util.generator.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DocumentFilter implements Filter {
  public static final String NAME = "extract";
  public static JsonNode document;

  public DocumentFilter(JsonNode doc) {
    document = doc;
  }

  public String docMD5Hash() {
    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      byte[] digest = hash.digest(document.asText().getBytes());
      return String.valueOf(Hex.encodeHex(digest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {
    if (object != null) {
      if (object.toString().equals("_hash_")) {
        return docMD5Hash();
      } else {
        return document.get(object.toString()).asText();
      }
    } else {
      return "";
    }
  }

  @Override
  public String getName() {
    return NAME;
  }
}
