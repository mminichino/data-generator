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

public class EntityGenerator {
  private static final Logger logger = LoggerFactory.getLogger(EntityGenerator.class);
  private String id;
  private Entity entity;
  private final long index;
  private final EntityDefinition definition;
  private final Timer recordTimer;
  private final Timer keyTimer;

  public EntityGenerator(EntityDefinition definition, long index, MeterRegistry registry) {
    this.definition = definition;
    this.index = index;
    this.recordTimer = Timer.builder("entity.generator.record.duration")
        .description("Time taken to generate a record")
        .tag("entity", "generator")
        .register(registry);
    this.keyTimer = Timer.builder("entity.generator.key.duration")
        .description("Time taken to generate a key")
        .tag("entity", "generator")
        .register(registry);
  }

  public Entity generate() throws Exception {
    entity = recordTimer.recordCallable(this::processMain);
    id = keyTimer.recordCallable(this::processId);
    entity.setId(id);
    logger.debug("Generated record: {}", id);
    return entity;
  }

  public Entity processMain() {
    try {
      Entity entity = new Entity();
      logger.debug("Generating table {} index {}", definition.getTableName(), index);
      FieldGenerator generator = new FieldGenerator();
      generator.init();
      List<Field> fields = definition.getFields().parallelStream()
          .map(definition -> generator.generate(definition, index))
          .toList();
      entity.setFields(fields);
      return entity;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String processId() {
    return keyTemplate(definition.keyFormat);
  }

  public Entity getEntity() {
    return entity;
  }

  public String getId() {
    return id;
  }

  public String keyTemplate(String template) {
    Context context = new Context();
    JinjavaConfig config = JinjavaConfig.newBuilder().build();
    Jinjava jinjava = new Jinjava(config);
    try {
      jinjava.getGlobalContext().registerFilter(new EntityGenerator.ZeroPadFilter());

      context.put("__uuid__", UUID.randomUUID().toString());
      context.put("__table__", definition.getTableName());

      entity.getFields().forEach(field ->
          context.put(field.getName(), field.getValue().toString()));

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
