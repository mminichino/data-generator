package com.codelry.util.generator.internal;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

public class CreateDatabase {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatabase.class);
  private static final Random rand = new Random();
  private static final String url = "jdbc:sqlite:src/main/resources/data/source.db";
  private static final String NAMES_TABLE = "CREATE TABLE IF NOT EXISTS names " +
                                            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                            "first TEXT, " +
                                            "last TEXT, " +
                                            "gender TEXT)";
  private static final String ADDRESS_TABLE = "CREATE TABLE IF NOT EXISTS addresses " +
                                              "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                              "number TEXT, " +
                                              "street TEXT, " +
                                              "city TEXT, " +
                                              "state TEXT, " +
                                              "zip TEXT)";
  private static final String AREA_CODE_TABLE = "CREATE TABLE IF NOT EXISTS areacodes " +
                                                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                "code TEXT, " +
                                                "state TEXT)";
  private static final String ZIP_CODE_TABLE = "CREATE TABLE IF NOT EXISTS zipcodes " +
                                               "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                               "city TEXT, " +
                                               "state TEXT, " +
                                               "zip TEXT, " +
                                               "plusfour TEXT)";
  private static final String STATE_TABLE = "CREATE TABLE IF NOT EXISTS states " +
                                            "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                            "state TEXT, " +
                                            "weight REAL)";
  private static final String PRODUCTS_TABLE = "CREATE TABLE IF NOT EXISTS products " +
                                               "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                               "department TEXT," +
                                               "manufacturer TEXT," +
                                               "category TEXT," +
                                               "subcategory TEXT," +
                                               "sku TEXT," +
                                               "name TEXT," +
                                               "seasonal INTEGER," +
                                               "price REAL, " +
                                               "cost REAL)";

  public CreateDatabase() {}

  public static void main(String[] args) {
    Options options = new Options();
    CommandLine cmd = null;
    configureLogging();

    Option nameOpt = new Option("n", "names", false, "Names");
    Option addressOpt = new Option("a", "address", false, "Addresses");
    Option phoneOpt = new Option("p", "phone", true, "Phones");
    Option zipOpt = new Option("z", "zip", true, "Zip Codes");
    Option stateOpt = new Option("s", "state", true, "State Data");
    Option productOpt = new Option("P", "products", false, "Products");

    nameOpt.setRequired(false);
    addressOpt.setRequired(false);
    phoneOpt.setRequired(false);
    zipOpt.setRequired(false);
    stateOpt.setRequired(false);
    productOpt.setRequired(false);

    options.addOption(nameOpt);
    options.addOption(addressOpt);
    options.addOption(phoneOpt);
    options.addOption(zipOpt);
    options.addOption(stateOpt);
    options.addOption(productOpt);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("simulator", options);
      System.exit(1);
    }

    LOGGER.info("Initializing database");
    try (Connection conn = DriverManager.getConnection(url)) {
      Statement stmt = conn.createStatement();
      stmt.execute(NAMES_TABLE);
      stmt.execute(ADDRESS_TABLE);
      stmt.execute(AREA_CODE_TABLE);
      stmt.execute(ZIP_CODE_TABLE);
      stmt.execute(STATE_TABLE);
      stmt.execute(PRODUCTS_TABLE);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    if (cmd.hasOption("names")) {
      populateNamesTable(10_000);
    } else if (cmd.hasOption("address")) {
      populateAddressTable(10_000);
    } else if (cmd.hasOption("phone")) {
      populatePhoneTable(cmd.getOptionValue("phone"));
    } else if (cmd.hasOption("zip")) {
      populateZipTable(cmd.getOptionValue("zip"));
    } else if (cmd.hasOption("state")) {
      populateStateTable(cmd.getOptionValue("state"));
    } else if (cmd.hasOption("products")) {
      populateProductsTable(1);
    }
  }

  private static void configureLogging() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.reset();

    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern("[%-5level] - %msg%n");
    encoder.start();

    ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
    appender.setContext(context);
    appender.setEncoder(encoder);
    appender.start();

    ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.ERROR);
    rootLogger.addAppender(appender);

    ch.qos.logback.classic.Logger appLogger = context.getLogger(CreateDatabase.class.getName());
    appLogger.setLevel(Level.INFO);
  }

  public static void populateNamesTable(int records) {
    int batch = 50;
    int count = (int) (Math.ceil((double) records / batch)) * batch;
    int iterations = count / batch;

    GenerateIdentityData gen = new GenerateIdentityData();

    LOGGER.info("Generating random name data");
    List<JsonNode> nameList = gen.generateNames(iterations, batch);

    LOGGER.info("Inserting name data into the database");
    try (Connection conn = DriverManager.getConnection(url)) {
      for (JsonNode name : nameList) {
        String sql = "INSERT INTO names(first,last,gender) VALUES(?,?,?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name.get("firstName").asText());
        stmt.setString(2, name.get("lastName").asText());
        stmt.setString(3, name.get("gender").asText());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void populateAddressTable(int records) {
    int batch = 50;
    int count = (int) (Math.ceil((double) records / batch)) * batch;
    int iterations = count / batch;

    GenerateIdentityData gen = new GenerateIdentityData();

    LOGGER.info("Generating random address data");
    List<JsonNode> addressList = gen.generateAddresses(iterations, batch);

    LOGGER.info("Inserting address data into the database");
    try (Connection conn = DriverManager.getConnection(url)) {
      for (JsonNode address : addressList) {
        String sql = "INSERT INTO addresses(number,street,city,state,zip) VALUES(?,?,?,?,?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, address.get("number").asText());
        stmt.setString(2, address.get("street").asText());
        stmt.setString(3, address.get("city").asText());
        stmt.setString(4, address.get("state").asText());
        stmt.setString(5, address.get("zipCode").asText());
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void populatePhoneTable(String filename) {
    LOGGER.info("Inserting phone data into the database");
    Set<String> uniqueCodes = new HashSet<>();
    try {
      try (Reader reader = new FileReader(filename); CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
        try (Connection conn = DriverManager.getConnection(url)) {
          for (CSVRecord record : parser) {
            if (uniqueCodes.contains(record.get(0))) {
              continue;
            }
            uniqueCodes.add(record.get(0));
            String sql = "INSERT INTO areacodes(code,state) VALUES(?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, record.get(0));
            stmt.setString(2, record.get(2));
            stmt.executeUpdate();
          }
        }
      }
    } catch (IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void populateZipTable(String filename) {
    LOGGER.info("Inserting zip code data into the database");
    try {
      try (Reader reader = new FileReader(filename); CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
        try (Connection conn = DriverManager.getConnection(url)) {
          for (CSVRecord record : parser) {
            String sql = "INSERT INTO zipcodes(city,state,zip,plusfour) VALUES(?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, WordUtils.capitalizeFully(record.get(7).toLowerCase()));
            stmt.setString(2, record.get(8));
            stmt.setString(3, record.get(9));
            stmt.setString(4, record.get(10));
            stmt.executeUpdate();
          }
        }
      }
    } catch (IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void populateStateTable(String filename) {
    LOGGER.info("Inserting state data into the database");
    try {
      try (Reader reader = new FileReader(filename); CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
        try (Connection conn = DriverManager.getConnection(url)) {
          for (CSVRecord record : parser) {
            String sql = "INSERT INTO states(state,weight) VALUES(?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(record.get(0)));
            stmt.setDouble(2, Double.parseDouble(record.get(1)));
            stmt.executeUpdate();
          }
        }
      }
    } catch (IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static float randomPercentage(int minValue, int maxValue) {
    int randomPercentage = rand.nextInt(maxValue - minValue + 1) + minValue;
    return randomPercentage / 100.0f;
  }

  public static Map<String, Integer> deptMap() {
    return Map.ofEntries(
        Map.entry("Apparel", 200),
        Map.entry("Electronics", 100),
        Map.entry("Home Furnishings", 100),
        Map.entry("Food and Beverage", 300),
        Map.entry("Health and Beauty", 100),
        Map.entry("Sports and Outdoors", 100),
        Map.entry("Toys and Games", 50),
        Map.entry("Media and Entertainment", 50),
        Map.entry("Automotive", 100),
        Map.entry("Baby and Kids", 100),
        Map.entry("Pet Supplies", 100),
        Map.entry("Office Supplies", 100),
        Map.entry("Garden and Outdoor", 100),
        Map.entry("Hardware and Tools", 100)
    );
  }

  public static void populateProductsTable(int scale) {
    int batch = 50;
    Map<String, Integer> departments = deptMap();

    GenerateIdentityData gen = new GenerateIdentityData();

    for (Map.Entry<String, Integer> entry : departments.entrySet()) {
      String department = entry.getKey();
      int base = entry.getValue();
      int count = base * scale;
      int iterations = count / batch;
      LOGGER.info("Generating random product data for department: {}", department);
      List<JsonNode> productList = gen.generateProducts(iterations, batch, department);

      LOGGER.info("Inserting {} data into the database", department);
      try (Connection conn = DriverManager.getConnection(url)) {
        for (JsonNode product : productList) {
          String sql = "INSERT INTO products(department,manufacturer,category,subcategory,sku,name,seasonal,price,cost) VALUES(?,?,?,?,?,?,?,?,?)";
          PreparedStatement stmt = conn.prepareStatement(sql);
          stmt.setString(1, department);
          stmt.setString(2, product.get("manufacturer").asText());
          stmt.setString(3, product.get("category").asText());
          stmt.setString(4, product.get("subcategory").asText());
          stmt.setString(5, product.get("sku").asText());
          stmt.setString(6, product.get("name").asText());
          stmt.setInt(7, product.get("seasonal").asBoolean() ? 1 : 0);
          stmt.setDouble(8, product.get("price").asDouble());
          float pct = randomPercentage(60, 80);
          double cost = product.get("price").asDouble() * pct;
          BigDecimal bdValue = new BigDecimal(cost);
          BigDecimal bdCost = bdValue.setScale(2, RoundingMode.HALF_UP);
          stmt.setDouble(9, bdCost.doubleValue());
          stmt.executeUpdate();
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
