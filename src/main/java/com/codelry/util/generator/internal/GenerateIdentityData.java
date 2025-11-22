package com.codelry.util.generator.internal;

import com.codelry.util.generator.util.JsonUtils;
import com.codelry.util.generator.util.ProgressOutput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.exception.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_5;

public class GenerateIdentityData {
  private static final Logger LOGGER = LogManager.getLogger(GenerateIdentityData.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeFactory typeFactory = mapper.getTypeFactory();
  static OpenAiChatModel model = OpenAiChatModel.builder()
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(GPT_5)
      .timeout(Duration.ofSeconds(180))
      .maxCompletionTokens(10240)
      .listeners(List.of(new TelemetryListener()))
      .build();

  public GenerateIdentityData() {}

  public List<JsonNode> generateNames(int iterations, int count) {
    return SimpleNameRecord.run(iterations, count);
  }

  public List<JsonNode> generateAddresses(int iterations, int count) {
    return SimpleAddressRecord.run(iterations, count);
  }

  public List<JsonNode> generateProducts(int iterations, int count, String department) {
    return ProductRecord.run(iterations, count, department);
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

    @StructuredPrompt("""
    Generate a list of exactly {{count}} unique synthetic product records as they might appear in an inventory management system as a JSON array.
    Select a product that would be found in the {{department}} department of a typical store.
    Return ONLY the JSON array, no other text.

    Each item must be an object:
    {
      department: (text) The product department,
      manufacturer: (text) The name of the company that makes the product,
      category: (text) The product category,
      subcategory: (text) The product subcategory,
      sku: (text) The product SKU,
      name: (text) THe product model or name,
      seasonal: (boolean) [true or false] If the product is a seasonal item,
      price: (float) The product price to the consumer,
    }
    """)
    public record CreateProductPrompt(int count, String department) {}

    public interface ProductService {
      String generateProducts(CreateProductPrompt prompt);
    }

    public static List<JsonNode> run(int iterations, int count, String department) {
      LOGGER.debug("Generating {} iterations {} count for department {}", iterations, count, department);
      List<JsonNode> productList = new ArrayList<>();
      int total = count * iterations;

      ProductService service = AiServices.builder(ProductService.class)
          .chatModel(model)
          .build();

      ProgressOutput progress = new ProgressOutput(total);
      progress.init();
      while (productList.size() < total) {
        try {
          LOGGER.debug("Sending product prompt to LLM");
          String answer = service.generateProducts(new CreateProductPrompt(count, department));
          if (answer == null) {
            progress.incrementErrorCount();
            LOGGER.debug("LLM returned null, retrying");
            continue;
          }
          List<JsonNode> batch = JsonUtils.parseJsonArray(answer);
          progress.writeLine(batch.size());
          productList.addAll(batch);
        } catch (TimeoutException t) {
          progress.incrementErrorCount();
          LOGGER.debug("Timeout in getting LLM response, retrying");
        } catch (JsonProcessingException e) {
          progress.incrementErrorCount();
          LOGGER.debug("Failed to parse LLM response, retrying");
        } catch (Exception e) {
          progress.newLine();
          LOGGER.error(e.getMessage(), e);
          return productList;
        }
      }

      progress.newLine();
      return new ArrayList<>(productList.subList(0, total));
    }
  }
}
