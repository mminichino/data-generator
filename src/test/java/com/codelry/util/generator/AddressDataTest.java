package com.codelry.util.generator;

import com.codelry.util.generator.internal.GenerateIdentityData;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AddressDataTest {
  private static final Logger LOGGER = LogManager.getLogger(AddressDataTest.class);

  @Test
  public void testAddressGeneration() {
    long startTime;
    long endTime;
    long duration;

    LOGGER.info("Starting test");
    GenerateIdentityData gen = new GenerateIdentityData();

    LOGGER.info("Generating names...");
    startTime = System.nanoTime();
    List<JsonNode> nameList = gen.generateNames(4,50);
    endTime = System.nanoTime();
    duration = (endTime - startTime) / 1000000;
    LOGGER.info("Names generated in {} ms", duration);

    LOGGER.info("Generating addresses...");
    startTime = System.nanoTime();
    List<JsonNode> addressList = gen.generateAddresses(4,50);
    endTime = System.nanoTime();
    duration = (endTime - startTime) / 1000000;
    LOGGER.info("Addresses generated in {} ms", duration);

    LOGGER.info("Name samples:");
    for (int i = 0; i < 3; i++) {
      System.out.println(nameList.get(i).toPrettyString());
    }
    LOGGER.info("Address samples:");
    for (int i = 0; i < 3; i++) {
      System.out.println(addressList.get(i).toPrettyString());
    }

    LOGGER.info("Name list length = {}", nameList.size());
    LOGGER.info("Address list length = {}", addressList.size());
  }
}
