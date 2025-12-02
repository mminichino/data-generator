package com.codelry.util.generator.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

public class DatabaseManager {
  private static final Logger LOGGER = LogManager.getLogger(DatabaseManager.class);
  private static DatabaseManager instance;
  public static long nameCount;
  public static long addressCount;
  public static long productCount;
  public static List<NameRecord> nameList = new ArrayList<>();
  public static List<AddressRecord> addressList = new ArrayList<>();
  public static List<ProductRecord> productList = new ArrayList<>();
  public static Map<String, List<String>> areaCodeList = new HashMap<>();
  public static Map<String, List<StateRecord>> stateList = new HashMap<>();
  public static LinkedHashMap<String, Double> stateMap = new LinkedHashMap<>();
  public static Map<String, Integer> nameLengthMap = new HashMap<>();
  public static Map<String, Integer> addressLengthMap = new HashMap<>();
  public static Map<String, Integer> productLengthMap = new HashMap<>();

  private DatabaseManager() {}

  public static DatabaseManager getInstance() {
    if (instance == null) {
      instance = new DatabaseManager();
      instance.init();
    }
    return instance;
  }

  public void init() {
    try {
      SQLiteConfig config = new SQLiteConfig();
      Properties properties = config.toProperties();
      URL sourceDb = DatabaseManager.class.getClassLoader().getResource("data/source.db");
      Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Objects.requireNonNull(sourceDb).getPath(), properties);
      buildNameList(conn);
      buildAddressList(conn);
      buildProductList(conn);
      buildAreaCodeList(conn);
      buildStateList(conn);
      buildStateMap(conn);
      buildNameLengthMap(conn);
      buildAddressLengthMap(conn);
      buildProductLengthMap(conn);
      nameCount = getNameCount();
      addressCount = getAddressCount();
      productCount = getProductCount();
      conn.close();
      LOGGER.debug("Database initialized");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public long getNameCount() {
    return nameList.size();
  }

  public long getAddressCount() {
    return addressList.size();
  }

  public long getProductCount() {
    return productList.size();
  }

  public void buildNameList(Connection conn) {
    String sql = "SELECT first, last, gender FROM names";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        nameList.add(new NameRecord(
            rs.getString("first"),
            rs.getString("last"),
            rs.getString("gender")
        ));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildAddressList(Connection conn) {
    String sql = "SELECT number, street, city, state, zip FROM addresses";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        addressList.add(new AddressRecord(
            rs.getString("number"),
            rs.getString("street"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("zip")
        ));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildProductList(Connection conn) {
    String sql = "SELECT department, manufacturer, category, subcategory, sku, name, seasonal, price, cost FROM products";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        productList.add(new ProductRecord(
            rs.getString("department"),
            rs.getString("manufacturer"),
            rs.getString("category"),
            rs.getString("subcategory"),
            rs.getString("sku"),
            rs.getString("name"),
            rs.getBoolean("seasonal"),
            rs.getFloat("price"),
            rs.getFloat("cost")
        ));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildAreaCodeList(Connection conn) {
    String sql = "SELECT state, code FROM areacodes";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        String state = rs.getString("state");
        String code = rs.getString("code");
        if (!areaCodeList.containsKey(state)) {
          areaCodeList.put(state, new ArrayList<>());
        }
        areaCodeList.get(state).add(code);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildStateList(Connection conn) {
    String getStates = "SELECT DISTINCT state FROM zipcodes";
    String getStateRecords = "SELECT * FROM zipcodes WHERE state = ?";
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(getStates);
      while (rs.next()) {
        String state = rs.getString("state");
        PreparedStatement pstmt = conn.prepareStatement(getStateRecords);
        pstmt.setString(1, state);
        ResultSet stateRs = pstmt.executeQuery();
        List<StateRecord> results = new ArrayList<>();
        while (stateRs.next()) {
          results.add(new StateRecord(
              stateRs.getString("city"),
              stateRs.getString("state"),
              stateRs.getString("zip"),
              stateRs.getString("plusfour")
          ));
        }
        stateList.put(state, results);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
    List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
    list.sort(Map.Entry.comparingByValue());

    LinkedHashMap<K, V> result = new LinkedHashMap<>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public void buildStateMap(Connection conn) {
    String sql = "SELECT state, weight FROM states";
    Map<String, Double> states = new HashMap<>();
    double totalWeight = 0.0;
    DecimalFormat df = new DecimalFormat("#.####");
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        String state = rs.getString("state");
        double weight = rs.getDouble("weight");
        states.put(state, weight);
      }
      stateMap = sortByValue(states);
      for (Map.Entry<String, Double> entry : stateMap.entrySet()) {
        totalWeight += entry.getValue();
        stateMap.replace(entry.getKey(), Double.valueOf(df.format(totalWeight)));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildNameLengthMap(Connection conn) {
    try {
      Statement stmt = conn.createStatement();
      int firstLength = stmt.executeQuery("SELECT MAX(LENGTH(first)) FROM names").getInt(1);
      int lastLength = stmt.executeQuery("SELECT MAX(LENGTH(last)) FROM names").getInt(1);
      nameLengthMap.put("first", firstLength);
      nameLengthMap.put("last", lastLength);
      nameLengthMap.put("fullName", firstLength + lastLength + 1);
      nameLengthMap.put("emailAddress", firstLength + lastLength + 1 + 12);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildAddressLengthMap(Connection conn) {
    try {
      Statement stmt = conn.createStatement();
      int streetLength = stmt.executeQuery("SELECT MAX(LENGTH(street)) FROM addresses").getInt(1);
      int cityLength = stmt.executeQuery("SELECT MAX(LENGTH(city)) FROM zipcodes").getInt(1);
      int stateLength = stmt.executeQuery("SELECT MAX(LENGTH(state)) FROM zipcodes").getInt(1);
      int zipLength = stmt.executeQuery("SELECT MAX(LENGTH(zip)) FROM zipcodes").getInt(1);
      addressLengthMap.put("street", streetLength);
      addressLengthMap.put("city", cityLength);
      addressLengthMap.put("state", stateLength);
      addressLengthMap.put("zip", zipLength);
      addressLengthMap.put("streetAddress", 6 + streetLength);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void buildProductLengthMap(Connection conn) {
    try {
      Statement stmt = conn.createStatement();
      int nameLength = stmt.executeQuery("SELECT MAX(LENGTH(name)) FROM products").getInt(1);
      int manufacturerLength = stmt.executeQuery("SELECT MAX(LENGTH(manufacturer)) FROM products").getInt(1);
      int categoryLength = stmt.executeQuery("SELECT MAX(LENGTH(category)) FROM products").getInt(1);
      int subcategoryLength = stmt.executeQuery("SELECT MAX(LENGTH(subcategory)) FROM products").getInt(1);
      int skuLength = stmt.executeQuery("SELECT MAX(LENGTH(sku)) FROM products").getInt(1);
      int departmentLength = stmt.executeQuery("SELECT MAX(LENGTH(department)) FROM products").getInt(1);
      productLengthMap.put("name", nameLength);
      productLengthMap.put("manufacturer", manufacturerLength);
      productLengthMap.put("category", categoryLength);
      productLengthMap.put("subcategory", subcategoryLength);
      productLengthMap.put("sku", skuLength);
      productLengthMap.put("department", departmentLength);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public String getState(double weight) {
    return stateMap.entrySet().stream()
        .filter(entry -> entry.getValue() >= weight)
        .findFirst()
        .map(Map.Entry::getKey)
        .orElseThrow(() -> new RuntimeException("No state found for weight " + weight));
  }

  public NameRecord getNameById(int id) {
    return nameList.get(id - 1);
  }

  public AddressRecord getAddressById(int id) {
    return addressList.get(id - 1);
  }

  public ProductRecord getProductById(int id) {
    return productList.get(id - 1);
  }

  public String getStreetNameById(int id) {
    return addressList.get(id - 1).street;
  }

  public List<StateRecord> getStateRecordsByState(String state) {
    return stateList.getOrDefault(state, new ArrayList<>());
  }

  public List<String> getAreaCodesByState(String state) {
    return areaCodeList.getOrDefault(state, new ArrayList<>());
  }

  public int getNameFieldLength(String field) {
    return nameLengthMap.getOrDefault(field, 16);
  }

  public int getAddressFieldLength(String field) {
    return addressLengthMap.getOrDefault(field, 128);
  }

  public int getProductFieldLength(String field) {
    return productLengthMap.getOrDefault(field, 256);
  }
}
