package com.codelry.util.generator.driver;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.generator.EntityLoad;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonData extends EntityLoad {
  private static final Logger logger = LoggerFactory.getLogger(JsonData.class);
  private final List<JsonNode> records = new ArrayList<>();

  public List<JsonNode> getRecords() {
    return records;
  }

  @Override
  public void prepare() {}

  @Override
  public void insertBatch(List<Entity> batch) {
    for (Entity record : batch) {
      logger.debug("Adding record {}", record.getId());
      records.add(record.asJson());
    }
  }

  @Override
  public void cleanup() {}
}
