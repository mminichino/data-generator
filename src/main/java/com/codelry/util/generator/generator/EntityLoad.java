package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class EntityLoad {
  private static final Logger LOGGER = LogManager.getLogger(EntityLoad.class);
  private static int batchSize = 5000;
  private long recordCount = 1;
  private long recordStart = 1;
  private boolean dynamicCount = false;
  public EntityCollection schema;
  public MeterRegistry registry;

  public void init(EntityCollection schema, long start) {
    this.schema = schema;
    this.recordCount = 0;
    this.dynamicCount = true;
    this.recordStart = start;
    this.registry = new SimpleMeterRegistry();
  }

  public void init(EntityCollection schema, long start, long count) {
    this.schema = schema;
    this.recordCount = count;
    this.recordStart = start;
    this.registry = new SimpleMeterRegistry();
  }

  public void init(EntityCollection schema, long start, long count, MeterRegistry registry) {
    this.schema = schema;
    this.recordCount = count;
    this.recordStart = start;
    this.registry = registry;
  }

  public void setBatchSize(int batchSize) {
    EntityLoad.batchSize = batchSize;
  }

  public abstract void prepare();

  public abstract void insertBatch(List<Entity> batch);

  public void generate() {
    generate(null);
  }

  public void generate(GenerationListener listener) {
    long totalRecords = computeTotalRecords();
    long completedRecords = 0;
    for (EntityDefinition definition : schema.getEntities()) {
      recordCount = dynamicCount ? definition.getCount() : recordCount;
      LOGGER.info("Generate start {} count {} for schema {}", recordStart, recordCount, schema.getName());
      definition.setNosql(schema.isNosql());
      EntityFactory factory = new EntityFactory(definition, recordStart, recordCount, registry);
      factory.setIndex(0);
      factory.start();
      for (int i = 0; i < recordCount; i += batchSize) {
        if (listener != null && listener.isCancelled()) {
          factory.stop();
          throw new GenerationCancelledException();
        }
        long end = Math.min(i + batchSize, recordCount);
        int chunk = (int) (end - i);
        LOGGER.debug("Inserting batch {} of {} records", i + 1, chunk);
        insertBatch(factory.collect(chunk));
        completedRecords += chunk;
        if (listener != null) {
          listener.onProgress(completedRecords, totalRecords);
        }
      }
      factory.stop();
    }
  }

  public long computeTotalRecords() {
    if (schema.getEntities() == null || schema.getEntities().isEmpty()) {
      return 0;
    }
    if (dynamicCount) {
      return schema.getEntities().stream().mapToLong(EntityDefinition::getCount).sum();
    }
    return recordCount * schema.getEntities().size();
  }

  public abstract void cleanup();
}
