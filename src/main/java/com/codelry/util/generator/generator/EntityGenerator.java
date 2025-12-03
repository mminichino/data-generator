package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.dto.EntityDefinition;
import com.codelry.util.generator.dto.Field;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EntityGenerator {
  private static final Logger logger = LoggerFactory.getLogger(EntityGenerator.class);
  private Entity entity;
  private final long index;
  private final EntityDefinition definition;
  private final Timer recordTimer;

  public EntityGenerator(EntityDefinition definition, long index, MeterRegistry registry) {
    this.definition = definition;
    this.index = index;
    this.recordTimer = Timer.builder("entity.generator.record.duration")
        .description("Time taken to generate a record")
        .tag("entity", "generator")
        .register(registry);
  }

  public Entity generate() throws Exception {
    entity = recordTimer.recordCallable(this::processMain);
    logger.debug("Generated record: {}", index);
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
      entity.setIndex(index);
      return entity;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Entity getEntity() {
    return entity;
  }
}
