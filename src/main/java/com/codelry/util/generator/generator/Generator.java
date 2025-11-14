package com.codelry.util.generator.generator;

import com.codelry.util.generator.randomizer.Randomizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import org.apache.commons.codec.binary.Hex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy.ECHO_UNDEFINED;

public class Generator {
  private final ObjectMapper mapper = new ObjectMapper();
  private final Randomizer randomizer = new Randomizer();
  private JinjavaInterpreter interpreter;
  private long indexValue;
  private String id;
  private JsonNode document;
  private final long index;
  private final String idTemplate;
  private final JsonNode docTemplate;
  private String idField = "id";

  public Generator(long number, String id, JsonNode doc) {
    index = number;
    idTemplate = id;
    docTemplate = doc;
  }

  public Record generate() {
    indexValue = index;
//    document = preProcess(docTemplate);
//    document = arrayProcess(document);
    document = processMain(docTemplate);
    id = processId(idTemplate);
    return new Record(getId(), getDocument());
  }

  public JsonNode preProcess(JsonNode json) {
    Context context = new Context();
    JinjavaConfig config = JinjavaConfig
        .newBuilder()
        .withFeatureConfig(
            FeatureConfig.newBuilder().add(ECHO_UNDEFINED, FeatureStrategies.ACTIVE).build()
        )
        .build();
    Jinjava jinjava = new Jinjava(config);
    try {
      String template = mapper.writeValueAsString(json);
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "repeat",
              this.getClass().getDeclaredMethod("arrayGen", int.class)
          )
      );
      interpreter = new JinjavaInterpreter(jinjava, context, config);
      return mapper.readTree(interpreter.render(template));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public JsonNode arrayProcess(JsonNode json) {
    ObjectNode result = mapper.createObjectNode();
    Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      result.set(field.getKey(), walkJson(field.getValue()));
    }
    return result;
  }

  public JsonNode processMain(JsonNode json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addSerializer(JsonNode.class, new GeneratorSerializer());
      mapper.registerModule(module);
      String template = mapper.writeValueAsString(json);
      return mapper.readTree(template);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public String processId(String template) {
    Context context = new Context();
    JinjavaConfig config = JinjavaConfig.newBuilder().build();
    Jinjava jinjava = new Jinjava(config);
    context.put("INDEX", indexValue);
    jinjava.getGlobalContext().registerFilter(new PadFilter());
    jinjava.getGlobalContext().registerFilter(new DocumentFilter(document));
    try {
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "random_uuid",
              this.getClass().getDeclaredMethod("randomUuid")
          )
      );
      return jinjava.render(template, context);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public JsonNode walkJson(JsonNode node) {
    if (node.isObject()) {
      return walkJson(node);
    } else if (node.isArray()) {
      if (node.get(0).asText().startsWith("####repeat:")) {
        String[] tag = node.get(0).asText().split(":");
        if (tag.length != 2) {
          throw new RuntimeException("Invalid repeat tag: " + node.get(0).asText());
        }
        int count = Integer.parseInt(tag[1]);
        JsonNode template = node.get(1).deepCopy();
        ArrayNode array = mapper.createArrayNode();
        for (int i = 1; i <= count; i++) {
          array.add(processMain(template));
        }
        return array;
      } else {
        return node;
      }
    } else {
      return node;
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
    return String.format("%0" + pad + "d", 11);
  }

  public String fieldValue(String field) {
    return document.has(field) ? document.get(field).asText() : "";
  }

  public String docHash() {
    try {
      MessageDigest hash = MessageDigest.getInstance("MD5");
      byte[] digest = hash.digest(document.asText().getBytes());
      return String.valueOf(Hex.encodeHex(digest));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static String randomSelection(Object list) {
    PyList elements = (PyList) list;
    Random rand = new Random();
    int index = rand.nextInt(elements.size());
    return elements.get(index).toString();
  }

  public static Integer randomNumber(int start, int end) {
    Random rand = new Random();
    return rand.nextInt((end - start) + 1) + start;
  }

  public static double randomDecimal(int start, int end, int precision) {
    Random rand = new Random();
    BigDecimal bd;
    double value = start + (end - start) * rand.nextDouble();
    bd = BigDecimal.valueOf(value);
    bd = bd.setScale(precision, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public static String arrayGen(int size) {
    return String.format("####repeat:%d", size);
  }

  public void setIdField(String field) {
    idField = field;
  }

  public JsonNode getDocument() {
    return document;
  }

  public String getId() {
    return id;
  }
}
