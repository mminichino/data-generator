package com.codelry.util.generator.generator;

import com.codelry.util.generator.db.AddressRecord;
import com.codelry.util.generator.db.NameRecord;
import com.codelry.util.generator.randomizer.Randomizer;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.ExpressionToken;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Render {
  private static Render instance;
  private final Randomizer randomizer = new Randomizer();
  private static final AtomicInteger index = new AtomicInteger(1);
  private static final Date date = new Date();

  private Render() {}

  public static Render getInstance() {
    if (instance == null) {
      instance = new Render();
    }
    return instance;
  }

  public String processTemplate(String template, int subIndex) {
    Context context = new Context();
    JinjavaConfig config = JinjavaConfig.newBuilder().build();
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
              "number",
              this.getClass().getDeclaredMethod("randomNumber", int.class, int.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "decimal",
              this.getClass().getDeclaredMethod("randomDecimal", int.class, int.class, int.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "random",
              this.getClass().getDeclaredMethod("randomSelection", Object.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "repeat",
              this.getClass().getDeclaredMethod("repeat", int.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "image_path",
              this.getClass().getDeclaredMethod("randomImagePath", int.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "date_string",
              this.getClass().getDeclaredMethod("randomDateString", String.class, int.class)
          )
      );
      jinjava.getGlobalContext().registerFunction(
          new ELFunctionDefinition(
              "",
              "timestamp",
              this.getClass().getDeclaredMethod("timestamp", int.class)
          )
      );
      Set<String> bindings = extractBindings(template);

      for (String binding : bindings) {
        switch (binding) {
          case "RANDOM_UUID":
            context.put("RANDOM_UUID", randomUuid());
            break;
          case "INDEX":
            context.put("INDEX", subIndex > 0 ? subIndex : index.getAndIncrement());
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
            context.put("ZIPCODE", "\"" + address.zip + "\"");
            context.put("PHONE_NUMBER", phoneNumber);
            break;
          case "BOOLEAN":
            context.put("BOOLEAN", randomizer.randomBoolean());
            break;
          case "CREDIT_CARD_CVV":
          case "CREDIT_CARD_NUMBER":
            context.put("CREDIT_CARD_NUMBER", "\"" + randomizer.randomCreditCardNumber() + "\"");
            context.put("CREDIT_CARD_CVV", "\"" + randomNumber(100, 999) + "\"");
            break;
          case "FUTURE_MONTH":
          case "FUTURE_YEAR":
            Date date = randomDate(5);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            context.put("FUTURE_MONTH", "\"" + monthFormat.format(date) + "\"");
            context.put("FUTURE_YEAR", "\"" + yearFormat.format(date) + "\"");
            break;
        }
      }
      JinjavaInterpreter interpreter = new JinjavaInterpreter(jinjava, context, config);
      return interpreter.render(template);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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

  public static String randomUuid() {
    return UUID.randomUUID().toString();
  }

  public static String indexNum(int pad) {
    pad = pad <= 0 ? 1 : pad;
    return String.format("%0" + pad + "d", index.getAndIncrement());
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

  public static int repeat(int size) {
    return size;
  }

  public static String randomImagePath(int nullable) {
    Random rand = new Random();
    boolean isNull = (nullable > 0) && rand.nextBoolean();
    return isNull ? "/v1/"
        + (rand.nextInt((1000000 - 1) + 1) + 1)
        + "/200/300/image.jpg" : "null";
  }

  public static Date randomDate(int offset) {
    Random rand = new Random();
    int seconds = rand.nextInt((15552000 - 86400) + 1) + 86400;
    int absOffset = Math.abs(offset);
    int delta = rand.nextInt((absOffset - 1) + 1) + 1;
    int years = offset > 0 ? delta : -delta;
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.YEAR, years);
    c.add(Calendar.SECOND, seconds);
    return c.getTime();
  }

  public static String randomDateString(String format, int offset) {
    Date time = randomDate(offset);
    SimpleDateFormat timeFormat = new SimpleDateFormat(format);
    return timeFormat.format(time);
  }

  public static String timestamp(int offset) {
    return randomDateString("yyyy-MM-dd'T'HH:mm:ss'Z'", offset);
  }
}
