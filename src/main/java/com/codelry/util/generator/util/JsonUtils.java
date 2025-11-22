package com.codelry.util.generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.List;

public class JsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static List<JsonNode> parseJsonArray(String jsonArray) throws JsonProcessingException {
    return MAPPER.readValue(
        jsonArray,
        TypeFactory.defaultInstance()
            .constructCollectionType(List.class, JsonNode.class)
    );
  }
}
