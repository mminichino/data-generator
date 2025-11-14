package com.codelry.util.generator.generator;

import com.codelry.util.generator.db.AddressRecord;
import com.codelry.util.generator.db.NameRecord;
import com.codelry.util.generator.randomizer.Randomizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DocIdGenerator {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final Randomizer randomizer = new Randomizer();
  private static JinjavaInterpreter interpreter;
  private static long indexValue;
  private static String templateId;
  private static String template;
  private static String id;
  private static JsonNode document;
  private static final Context context = new Context();

  public DocIdGenerator(long index, String idTemplate, JsonNode docTemplate) {
    indexValue = index;
    templateId = idTemplate;
    template = extractDocTemplate(docTemplate);
    JinjavaConfig config = new JinjavaConfig();
    Jinjava jinjava = new Jinjava(config);
    try {
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "random_uuid",
              this.getClass().getDeclaredMethod("randomUuid")
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "index",
              this.getClass().getDeclaredMethod("indexNum", int.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "field",
              this.getClass().getDeclaredMethod("fieldValue", String.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "doc_hash",
              this.getClass().getDeclaredMethod("docHash")
          )
      );
      Set<String> bindings = extractBindings(template);

      for (String binding : bindings) {
        switch (binding) {
          case "RANDOM_UUID":
            context.put("RANDOM_UUID", randomUuid());
            break;
          case "INDEX":
            context.put("INDEX", indexValue);
            break;
          case "FIRST_NAME":
          case "LAST_NAME":
          case "FULL_NAME":
          case "EMAIL_ADDRESS":
            NameRecord name = randomizer.randomNameRecord();
            context.put("FIRST_NAME", name.first);
            context.put("LAST_NAME", name.last);
            context.put("FULL_NAME", name.fullName());
            context.put("EMAIL_ADDRESS", name.emailAddress());
            break;
          case "ADDRESS_LINE_1":
          case "CITY":
          case "STATE":
          case "ZIPCODE":
          case "PHONE_NUMBER":
            AddressRecord address = randomizer.randomAddressRecord();
            String phoneNumber = randomizer.randomPhoneNumber(address.state);
            context.put("ADDRESS_LINE_1", address.number + " " + address.street);
            context.put("CITY", address.city);
            context.put("STATE", address.state);
            context.put("ZIPCODE", address.zip);
            context.put("PHONE_NUMBER", phoneNumber);
            break;
        }
      }
      interpreter = new JinjavaInterpreter(jinjava, context, config);

      try {
        String renderedTemplate = interpreter.render(template);
        document = mapper.readTree(renderedTemplate);
      } catch (Exception e) {
        throw new RuntimeException("Problem rendering document template: " + e.getMessage(), e);
      }

      id = interpreter.render(templateId);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public String extractDocTemplate(JsonNode templateJson) {
    try {
      return mapper.writeValueAsString(templateJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Set<String> extractBindings(String template) {
    Set<String> bindings = new HashSet<>();
    try {
      Jinjava jinjava = new Jinjava();
      JinjavaInterpreter interpreter = jinjava.newInterpreter();
      Node rootNode = interpreter.parse(template);
      traverseNodes(rootNode, bindings);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return bindings;
  }

  private void traverseNodes(Node node, Set<String> bindings) {
    if (node instanceof ExpressionNode) {
      ExpressionToken token = (ExpressionToken) node.getMaster();
      bindings.add(token.getExpr());
    }
    for (Node child : node.getChildren()) {
      traverseNodes(child, bindings);
    }
  }

  public static String randomUuid() {
    return UUID.randomUUID().toString();
  }

  public static String indexNum(int pad) {
    pad = pad <= 0 ? 1 : pad;
    return String.format("%0" + pad + "d", indexValue);
  }

  public static String fieldValue(String field) {
    return document.has(field) ? document.get(field).asText() : "";
  }

  public static String docHash() {
    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      byte[] digest = hash.digest(document.asText().getBytes());
      return String.valueOf(Hex.encodeHex(digest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonNode getDocument() {
    return document;
  }

  public String getId() {
    return id;
  }
}
