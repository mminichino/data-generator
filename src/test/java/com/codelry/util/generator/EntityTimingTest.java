package com.codelry.util.generator;

import com.codelry.util.generator.dto.*;
import com.codelry.util.generator.generator.EntityLoad;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EntityTimingTest {
  private static final Logger logger = LoggerFactory.getLogger(EntityTimingTest.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static EntityCollection schema;

  public static class TestDriver extends EntityLoad {
    private static final Logger logger = LoggerFactory.getLogger(TestDriver.class);
    private final List<Entity> records = new ArrayList<>();

    public List<Entity> getRecords() {
      return records;
    }

    @Override
    public void prepare() {}

    @Override
    public void insertBatch(List<Entity> batch) {
      for (Entity entity : batch) {
        logger.debug("Adding record {}", entity.getId());
        records.add(entity);
      }
    }

    @Override
    public void cleanup() {}
  }

  @BeforeAll
  public static void setUp() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();

    try {
      schema = mapper.readValue(loader.getResourceAsStream("schema.json"), EntityCollection.class);
    } catch (IOException e) {
      System.out.println("can not open template file: " + e.getMessage());
      e.printStackTrace(System.err);
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDriverBasic() {
    MeterRegistry registry = new SimpleMeterRegistry();
    TestDriver driver = new TestDriver();
    driver.init(schema, 1, 1000, registry);
    driver.generate();
    Timer recordTimer = registry.find("entity.generator.record.duration")
        .tags("entity", "generator")
        .timer();
    Timer keyTimer = registry.find("entity.generator.key.duration")
        .tags("entity", "generator")
        .timer();
    Assertions.assertNotNull(recordTimer);
    logger.info("Total records: {}", recordTimer.count());
    logger.info("Total time  : {}", recordTimer.totalTime(TimeUnit.MILLISECONDS));
    logger.info("Average time: {}", recordTimer.mean(TimeUnit.MILLISECONDS));
    logger.info("Max time    : {}", recordTimer.max(TimeUnit.MILLISECONDS));
    Assertions.assertNotNull(keyTimer);
    logger.info("Total time  : {}", keyTimer.totalTime(TimeUnit.MILLISECONDS));
    logger.info("Average time: {}", keyTimer.mean(TimeUnit.MILLISECONDS));
    logger.info("Max time    : {}", keyTimer.max(TimeUnit.MILLISECONDS));
  }
}
