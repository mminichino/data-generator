package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.dto.Field;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EntitySerializer extends JsonSerializer<Entity> {

  @Override
  public void serialize(Entity entity, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    generateJson(entity, gen);
  }

  public static void generateJson(Entity entity, JsonGenerator gen) throws IOException {
    gen.writeStartObject();
    for (Field field : entity.getFields()) {
      gen.writeFieldName(field.getName());
      if (field.value instanceof Integer) {
        gen.writeNumber((Integer) field.value);
      } else if (field.value instanceof Long) {
        gen.writeNumber((Long) field.value);
      } else if (field.value instanceof Double) {
        gen.writeNumber((Double) field.value);
      } else if (field.value instanceof Boolean) {
        gen.writeBoolean((Boolean) field.value);
      } else if (field.value instanceof List) {
        gen.writeStartArray();
        for (Object member : (List<?>) field.value) {
          gen.writeString(String.valueOf(member));
        }
        gen.writeEndArray();
      } else if (field.value instanceof Date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        gen.writeString(timeFormat.format((Date) field.value));
      } else {
        gen.writeString((String) field.value);
      }
    }
    gen.writeEndObject();
  }
}
