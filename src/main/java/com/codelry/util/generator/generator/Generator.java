package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.lib.filter.Filter;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {
  private static final Logger logger = LoggerFactory.getLogger(Generator.class);
  private String id;
  private JsonNode document;
  private final Table table;

  public Generator(Table table) {
    this.table = table;
  }

  public Record generate() {
    document = processMain();
    id = processId();
    logger.debug("Generated record: {}", id);
    return new Record(getId(), getDocument());
  }

  public JsonNode processMain() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addSerializer(Table.class, new GeneratorSerializer());
      mapper.registerModule(module);
      String template = mapper.writeValueAsString(table);
      logger.debug("Generating table {} index {}", table.getName(), table.getIndex().get());
      return mapper.readTree(template);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public String processId() {
    return keyTemplate(table.keyFormat);
  }

  public JsonNode getDocument() {
    return document;
  }

  public String getId() {
    return id;
  }

  public String keyTemplate(String template) {
    Context context = new Context();
    JinjavaConfig config = JinjavaConfig.newBuilder().build();
    Jinjava jinjava = new Jinjava(config);
    try {
      jinjava.getGlobalContext().registerFilter(new Generator.ZeroPadFilter());

      context.put("__uuid__", UUID.randomUUID().toString());
      context.put("__table__", table.name);

      document.properties().forEach(entry ->
          context.put(entry.getKey(), entry.getValue().asText()));

      RenderResult result = jinjava.renderForResult(template, context);
      if (!result.getErrors().isEmpty()) {
        result.getErrors().forEach(error ->
            logger.error("Error rendering key template: [{}] {}", error.getSeverity(), error.getReason()));
      }
      return result.getOutput();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class ZeroPadFilter implements Filter {

    @Override
    public String getName() {
      return "zero_pad";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      if (var == null) {
        return "";
      }

      int targetLength = 10;
      if (args.length > 0) {
        try {
          targetLength = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
          return var;
        }
      }

      String value = var.toString();

      if (value.length() >= targetLength) {
        return value;
      }

      return String.format("%0" + targetLength + "d", Integer.parseInt(value));
    }
  }
}
