package com.codelry.util.generator;

import com.codelry.util.generator.internal.GenerateIdentityData;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import java.util.List;

public class ProductDataTest {
  private static final Logger LOGGER = LogManager.getLogger(ProductDataTest.class);

  @Test
  public void testAddressGeneration() {
    long startTime;
    long endTime;
    long duration;
    String[] departments = {
        "Apparel",
        "Electronics",
        "Home Furnishings",
        "Food and Beverage",
        "Health and Beauty",
        "Sports and Outdoors",
        "Toys and Games",
        "Media and Entertainment",
        "Automotive",
        "Baby and Kids",
        "Pet Supplies",
        "Office Supplies",
        "Garden and Outdoor",
        "Hardware and Tools",
    };

    LOGGER.info("Starting test");
    GenerateIdentityData gen = new GenerateIdentityData();

    for (String department : departments) {
      List<JsonNode> productList;
      LOGGER.info("Generating products for {}", department);
      startTime = System.nanoTime();
      productList = gen.generateProducts(1, 50, department);
      endTime = System.nanoTime();
      duration = (endTime - startTime) / 1000000;
      LOGGER.info("Products generated in {} ms", duration);

      LOGGER.info("Product samples:");
      for (int i = 0; i < 3; i++) {
        LOGGER.info("#{}:\n{}", i + 1, productList.get(i).toPrettyString());
      }
      LOGGER.info("Product list length = {}", productList.size());
    }
  }
}
