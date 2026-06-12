package com.codelry.util.generator.internal;

import com.codelry.util.generator.util.JsonUtils;
import com.codelry.util.generator.util.ProgressOutput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateIdentityData {
  private static final Logger LOGGER = LogManager.getLogger(GenerateIdentityData.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeFactory typeFactory = mapper.getTypeFactory();
  static OpenAiChatModel model = OpenAiChatModel.builder()
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName("gpt-5.4")
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

  public List<JsonNode> generateAirports() {
    return AirportRecord.run();
  }

  public List<JsonNode> generateAirlines() {
    return AirlineRecord.run();
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

  static class AirportRecord {

    private static String createAirportPrompt() {
      return """
          Generate a list of commercial airports that support passenger travel as a JSON array.
          Only include airports that support scheduled flight services to the general public.
          Do not include any airports that only support private aviation or do not have gates supporting commercial carriers.
          Return ONLY the JSON array, no other text.

          Each item must be an object:
          {
            iata: (text) The three character IATA airport code,
            city: (text) The city served by the airport,
            name: (text) The official airport name,
          }

          Return as many airports as fit within your response limit, ordered alphabetically by IATA code.
          """;
    }

    private static String continueAirportPrompt(String lastIataCode) {
      return """
          Continue generating commercial airports that support passenger travel as a JSON array.
          Only include airports with IATA codes alphabetically after %s.
          Only include airports that support scheduled flight services to the general public.
          Do not include any airports that only support private aviation or do not have gates supporting commercial carriers.
          Return ONLY the JSON array, no other text. If there are no more airports to add, return an empty JSON array [].

          Each item must be an object:
          {
            iata: (text) The three character IATA airport code,
            city: (text) The city served by the airport,
            name: (text) The official airport name,
          }

          Continue alphabetically by IATA code.
          """.formatted(lastIataCode);
    }

    public static List<JsonNode> run() {
      LOGGER.debug("Generating airport records");
      List<JsonNode> airportList = new ArrayList<>();
      Set<String> seenCodes = new HashSet<>();

      boolean firstBatch = true;
      String lastIataCode = "";
      while (true) {
        try {
          LOGGER.debug("Sending airport prompt to LLM");
          String prompt = firstBatch
              ? createAirportPrompt()
              : continueAirportPrompt(lastIataCode);

          String answer = model.chat(prompt);
          if (answer == null) {
            LOGGER.debug("LLM returned null generating airports, retrying");
            continue;
          }

          firstBatch = false;

          List<JsonNode> batch = JsonUtils.parseJsonArray(answer);
          if (batch.isEmpty()) {
            break;
          }

          int added = 0;
          for (JsonNode airport : batch) {
            JsonNode iataNode = airport.get("iata");
            if (iataNode != null && iataNode.isTextual() && seenCodes.add(iataNode.asText())) {
              airportList.add(airport);
              lastIataCode = iataNode.asText();
              added++;
            }
          }

          LOGGER.debug("Added {} airports ({} total)", added, airportList.size());

          if (added == 0) {
            break;
          }
        } catch (TimeoutException t) {
          LOGGER.debug("Timeout in getting LLM airports response, retrying");
        } catch (JsonProcessingException e) {
          LOGGER.debug("Failed to parse LLM airports response, retrying");
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          return airportList;
        }
      }

      return airportList;
    }
  }

  static class AirlineRecord {

    private static String createAirlinePrompt() {
      return """
          Generate a list of commercial airlines that support passenger travel as a JSON array.
          Only include airlines that support scheduled flight services to the general public.
          Do not include any airlines that only provide private aviation or do not have gates supporting commercial passengers.
          Return ONLY the JSON array, no other text.

          Each item must be an object:
          {
            iata: (text) The two character IATA airline code,
            name: (text) The official airline name
          }

          Return as many airlines as fit within your response limit, ordered alphabetically by IATA code.
          """;
    }

    private static String continueAirlinePrompt(String lastIataCode) {
      return """
          Continue generating commercial airlines that support passenger travel as a JSON array.
          Only include airlines with IATA codes alphabetically after %s.
          Only include airlines that provide scheduled flight services to the general public.
          Do not include any airlines that only provide private aviation or do not have gates supporting commercial passengers.
          Return ONLY the JSON array, no other text. If there are no more airlines to add, return an empty JSON array [].

          Each item must be an object:
          {
            iata: (text) The two character IATA airline code,
            name: (text) The official airline name,
          }

          Continue alphabetically by IATA code.
          """.formatted(lastIataCode);
    }

    public static List<JsonNode> run() {
      LOGGER.debug("Generating airline records");
      List<JsonNode> airlineList = new ArrayList<>();
      Set<String> seenCodes = new HashSet<>();

      boolean firstBatch = true;
      String lastIataCode = "";
      while (true) {
        try {
          LOGGER.debug("Sending airline prompt to LLM");
          String prompt = firstBatch
              ? createAirlinePrompt()
              : continueAirlinePrompt(lastIataCode);

          String answer = model.chat(prompt);
          if (answer == null) {
            LOGGER.debug("LLM returned null generating airline, retrying");
            continue;
          }

          firstBatch = false;

          List<JsonNode> batch = JsonUtils.parseJsonArray(answer);
          if (batch.isEmpty()) {
            break;
          }

          int added = 0;
          for (JsonNode airline : batch) {
            JsonNode iataNode = airline.get("iata");
            if (iataNode != null && iataNode.isTextual() && seenCodes.add(iataNode.asText())) {
              airlineList.add(airline);
              lastIataCode = iataNode.asText();
              added++;
            }
          }

          LOGGER.debug("Added {} airline ({} total)", added, airlineList.size());

          if (added == 0) {
            break;
          }
        } catch (TimeoutException t) {
          LOGGER.debug("Timeout in getting LLM airline response, retrying");
        } catch (JsonProcessingException e) {
          LOGGER.debug("Failed to parse LLM airline response, retrying");
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          return airlineList;
        }
      }

      return airlineList;
    }
  }
}
