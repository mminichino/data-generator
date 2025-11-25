package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Table;
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
  private final Table table;
  private String idField = "id";

  public Generator(long index, Table table) {
    this.index = index;
    this.table = table;
    this.table.setIndex(index);
  }

  public Record generate() {
    indexValue = index;
    document = processMain();
    id = processId();
    return new Record(getId(), getDocument());
  }

  public JsonNode processMain() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addSerializer(Table.class, new GeneratorSerializer());
      mapper.registerModule(module);
      String template = mapper.writeValueAsString(table);
      return mapper.readTree(template);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public String processId() {
    return "id_" + indexValue + "_" + table.getName();
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
