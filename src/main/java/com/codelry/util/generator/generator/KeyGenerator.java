package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.dto.EntityDefinition;
import com.codelry.util.generator.dto.Field;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.lib.filter.Filter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class KeyGenerator {
  private static final Logger logger = LoggerFactory.getLogger(KeyGenerator.class);
  private final String keyFormat;
  private final String tableName;
  private final JinjavaConfig config = JinjavaConfig.newBuilder().build();
  private final Jinjava jinjava = new Jinjava(config);
  private final Timer keyTimer;

  public KeyGenerator(EntityDefinition definition, MeterRegistry registry) {
    this.keyFormat = definition.getKeyFormat();
    this.tableName = definition.getTableName();
    this.keyTimer = Timer.builder("entity.generator.key.duration")
        .description("Time taken to generate a key")
        .tag("entity", "generator")
        .register(registry);
    this.jinjava.getGlobalContext().registerFilter(new KeyGenerator.ZeroPadFilter());
  }

  public String generate(Entity entity) {
    try {
      return keyTimer.recordCallable(() -> render(entity));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String render(Entity entity) {
    Context context = new Context();
    try {
      context.put("__uuid__", UUID.randomUUID().toString());
      context.put("__table__", tableName);
      context.put("__index__", entity.getIndex());

      entity.getFields().forEach(field ->
          context.put(field.getName(), field.getValue().toString()));

      RenderResult result = jinjava.renderForResult(keyFormat, context);
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
