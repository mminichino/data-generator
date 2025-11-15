package com.codelry.util.generator.internal;

import com.codelry.util.generator.util.ProgressOutput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_5;
import static java.time.Duration.ofSeconds;

public class GenerateIdentityData {
  private static final Logger LOGGER = LogManager.getLogger(GenerateIdentityData.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeFactory typeFactory = mapper.getTypeFactory();
  static OpenAiChatModel model = OpenAiChatModel.builder()
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(GPT_5)
      .timeout(ofSeconds(180))
      .maxTokens(4096)
      .build();

  public GenerateIdentityData() {}

  public List<JsonNode> generateNames(int iterations, int count) {
    return SimpleNameRecord.run(iterations, count);
  }

  public List<JsonNode> generateAddresses(int iterations, int count) {
    return SimpleAddressRecord.run(iterations, count);
  }

  static class SimpleNameRecord {

    @StructuredPrompt({
        "Generate {{count}} unique synthetic first and last name data records using names commonly found in the United States.",
        "Come up with real names, and never use most popular placeholders like john doe and jane doe.",
        "Structure your answer as a JSON list without any markdown with each JSON object formatted in the following way:",

        "{",
        "firstName: The person's first name",
        "lastName: The person's surname",
        "gender: The person's gender denoted as male or female",
        "}"
    })
    static class CreateNamePrompt {
      private int count;
    }

    interface Assistant {
      String chat(String message);
    }

    public static List<JsonNode> run(int iterations, int count) {
      ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
      List<JsonNode> nameList = new ArrayList<>();
      int total = count * iterations;

      CreateNamePrompt namePrompt = new CreateNamePrompt();
      namePrompt.count = count;
      Prompt prompt = StructuredPromptProcessor.toPrompt(namePrompt);

      Assistant assistant = AiServices.builder(Assistant.class)
          .chatModel(model)
          .chatMemory(chatMemory)
          .build();

      ProgressOutput progress = new ProgressOutput(total);
      progress.init();
      while (nameList.size() < total) {
        try {
          String answer = assistant.chat(prompt.toUserMessage().toString());
          List<JsonNode> batch = mapper.readValue(answer, typeFactory.constructCollectionType(List.class, JsonNode.class));
          progress.writeLine(batch.size());
          nameList.addAll(batch);
        } catch (Exception e) {
          progress.incrementErrorCount();
          LOGGER.debug(e.getMessage(), e);
        }
      }
      progress.newLine();
      return new ArrayList<>(nameList.subList(0, total));
    }
  }

  static class SimpleAddressRecord {

    @StructuredPrompt({
        "Generate {{count}} unique synthetic mailing address records located in the United States.",
        "Structure your answer as a JSON list without any markdown with each JSON object formatted in the following way:",

        "{",
        "number: The address number",
        "street: The address street name and street suffix",
        "city: The address city",
        "state: The address state standard abbreviation",
        "zipCode: THe address five digit zip code",
        "}"
    })
    static class CreateAddressPrompt {
      private int count;
    }

    interface Assistant {
      String chat(String message);
    }

    public static List<JsonNode> run(int iterations, int count) {
      ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
      List<JsonNode> addressList = new ArrayList<>();
      int total = count * iterations;

      CreateAddressPrompt addressPrompt = new CreateAddressPrompt();
      addressPrompt.count = count;
      Prompt prompt = StructuredPromptProcessor.toPrompt(addressPrompt);

      Assistant assistant = AiServices.builder(Assistant.class)
          .chatModel(model)
          .chatMemory(chatMemory)
          .build();

      ProgressOutput progress = new ProgressOutput(total);
      progress.init();
      while (addressList.size() < total) {
        try {
          String answer = assistant.chat(prompt.toUserMessage().toString());
          List<JsonNode> batch = mapper.readValue(answer, typeFactory.constructCollectionType(List.class, JsonNode.class));
          progress.writeLine(batch.size());
          addressList.addAll(batch);
        } catch (Exception e) {
          progress.incrementErrorCount();
          LOGGER.debug(e.getMessage(), e);
        }
      }
      progress.newLine();
      return new ArrayList<>(addressList.subList(0, total));
    }
  }

  static class ProductRecord {

    @StructuredPrompt({
        "Generate {{count}} unique synthetic product records as they might appear in an inventory management system. ",
        "Use the following departments: ",
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
        "While this is a synthetic data set, use real manufacturers, and real products based on available public data. ",
        "Create synthetic SKUs following a pattern that could be seen in a real inventory system.",
        "For the price use realistic list prices if publicly available or a reasonable guess. ",
        "Use a reasonable guess for the cost. ",
        "Structure your answer as a JSON list without any markdown with each JSON object formatted in the following way: ",

        "{",
        "department: (text) The product department",
        "manufacturer: (text) The name of the company that makes the product",
        "category: (text) The product category",
        "subcategory: (text) The product subcategory",
        "sku: (text) The product SKU",
        "name: (text) THe product model or name",
        "seasonal: (boolean) [true or false] If the product is a seasonal item",
        "price: (float) The product price to the consumer",
        "cost: (float) The cost to produce or manufacture the product",
        "}"
    })
    static class CreateProductPrompt {
      private int count;
    }

    interface Assistant {
      String chat(String message);
    }

    public static List<JsonNode> run(int iterations, int count) {
      ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
      List<JsonNode> productList = new ArrayList<>();
      int total = count * iterations;

      CreateProductPrompt productPrompt = new CreateProductPrompt();
      productPrompt.count = count;
      Prompt prompt = StructuredPromptProcessor.toPrompt(productPrompt);

      Assistant assistant = AiServices.builder(Assistant.class)
          .chatModel(model)
          .chatMemory(chatMemory)
          .build();

      ProgressOutput progress = new ProgressOutput(total);
      progress.init();
      while (productList.size() < total) {
        try {
          String answer = assistant.chat(prompt.toUserMessage().toString());
          List<JsonNode> batch = mapper.readValue(answer, typeFactory.constructCollectionType(List.class, JsonNode.class));
          progress.writeLine(batch.size());
          productList.addAll(batch);
        } catch (Exception e) {
          progress.incrementErrorCount();
          LOGGER.debug(e.getMessage(), e);
        }
      }
      progress.newLine();
      return new ArrayList<>(productList.subList(0, total));
    }
  }
}
