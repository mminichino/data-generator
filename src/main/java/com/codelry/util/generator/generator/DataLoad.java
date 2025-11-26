package com.codelry.util.generator.generator;

import java.util.List;
import com.codelry.util.generator.dto.SchemaCollection;
import com.codelry.util.generator.dto.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DataLoad {
  private static final Logger LOGGER = LogManager.getLogger(DataLoad.class);
  private static int batchSize = 100;
  private static long recordCount = 1;
  private static long recordStart = 1;
  public SchemaCollection schema;

  public void init(SchemaCollection schema, long start, long count) {
    this.schema = schema;
    recordCount = count;
    recordStart = start;
  }

  public void setBatchSize(int batchSize) {
    DataLoad.batchSize = batchSize;
  }

  public abstract void prepare();

  public abstract void insertBatch(List<Record> batch);

  public void generate() {
    LOGGER.info("Generate start {} count {}", recordStart, recordCount);
    for (Table table : schema.getTables()) {
      LOGGER.info("Generating data for schema {}", schema.getName());
      RecordFactory factory = new RecordFactory(table, recordStart, recordCount);
      factory.setIndex(0);
      factory.start();
      for (int i = 0; i < recordCount; i += batchSize) {
        long end = Math.min(i + batchSize, recordCount);
        int chunk = (int) (end - i);
        LOGGER.debug("Inserting batch {} of {} records", i + 1, chunk);
        insertBatch(factory.collect(chunk));
      }
      factory.stop();
    }
  }

  public abstract void cleanup();
}
