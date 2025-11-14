package com.codelry.util.generator.generator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class GeneratorSerializer extends JsonSerializer<JsonNode> {
  private static final Logger LOGGER = LogManager.getLogger(GeneratorSerializer.class);

  @Override
  public void serialize(JsonNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    generateJson(value, gen, 0);
  }

  public static void generateJson(JsonNode node, JsonGenerator gen, int index) throws IOException {
    if (node.isObject()) {
      gen.writeStartObject();
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        String fieldName = field.getKey();
        JsonNode fieldValue = field.getValue();
        gen.writeFieldName(fieldName);
        generateJson(fieldValue, gen, index);
      }
      gen.writeEndObject();
    } else if (node.isArray()) {
      gen.writeStartArray();
      if (node.get(0).isTextual()) {
        Render render = Render.getInstance();
        String result = render.processTemplate(node.get(0).asText(), index);
        try {
          int count = Integer.parseInt(result);
          for (int i = 0; i < count; i++) {
            generateJson(node.get(1), gen, i + 1);
          }
        } catch (NumberFormatException e) {
          for (JsonNode arrayElement : node) {
            generateJson(arrayElement, gen, index);
          }
        }
      } else {
        for (JsonNode arrayElement : node) {
          generateJson(arrayElement, gen, index);
        }
      }
      gen.writeEndArray();
    } else if (node.isTextual()) {
      Render render = Render.getInstance();
      String result = render.processTemplate(node.asText(), index);

      try {
        int intResult = Integer.parseInt(result);
        gen.writeNumber(intResult);
        return;
      } catch (NumberFormatException ignored) {
      }

      try {
        long longResult = Long.parseLong(result);
        gen.writeNumber(longResult);
        return;
      } catch (NumberFormatException ignored) {
      }

      try {
        double doubleResult = Double.parseDouble(result);
        gen.writeNumber(doubleResult);
        return;
      } catch (NumberFormatException ignored) {
      }

      if (result.equalsIgnoreCase("true") || result.equalsIgnoreCase("false")) {
        boolean boolResult = Boolean.parseBoolean(result);
        gen.writeBoolean(boolResult);
        return;
      }

      if (result.equalsIgnoreCase("null")) {
        gen.writeNull();
        return;
      }

      result = result.replace("\"", "");
      gen.writeString(result);
    } else if (node.isInt()) {
      gen.writeNumber(node.asInt());
    } else if (node.isBoolean()) {
      gen.writeBoolean(node.asBoolean());
    } else if (node.isNull()) {
      gen.writeNull();
    }
  }
}
