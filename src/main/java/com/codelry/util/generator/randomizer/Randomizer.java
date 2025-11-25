package com.codelry.util.generator.randomizer;

import com.codelry.util.generator.db.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Randomizer {
  private final Random rand = new Random();
  private static DatabaseManager databaseManager;
  private static final String[] lorem = {
      "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing",
      "elit", "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore",
      "et", "dolore", "magna", "aliqua"
  };
  private static final String[] terminator = {".", "?", "!"};
  private static final String[] punctuation = {";", ":", ","};

  public Randomizer() {
    databaseManager = DatabaseManager.getInstance();
  }

  public void randomBytes(byte[] bytes) {
    rand.nextBytes(bytes);
  }

  public String randomUuid() {
    return UUID.randomUUID().toString();
  }

  public int randomNumber(int minValue, int maxValue) {
    return rand.nextInt((maxValue - minValue) + 1) + minValue;
  }

  public double roundDouble(double value, int places) {
    BigDecimal bd;
    bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public double randomNumber(int digits, boolean isDecimal) {
    int min = (int) Math.pow(10, digits - 1);
    int max = (int) Math.pow(10, digits) - 1;
    int places = isDecimal ? digits : 0;
    return randomDouble(min, max, places);
  }

  public double randomDollarAmount(int digits) {
    int min = (int) Math.pow(10, digits - 1);
    int max = (int) Math.pow(10, digits) - 1;
    int places = 2;
    return randomDouble(min, max, places);
  }

  public double randomDouble(double minValue, double maxValue, int places) {
    double randomValue = minValue + (maxValue - minValue) * rand.nextDouble();
    return roundDouble(randomValue, places);
  }

  public float randomPercentage(int minValue, int maxValue) {
    int randomPercentage = rand.nextInt(maxValue - minValue + 1) + minValue;
    return randomPercentage / 100.0f;
  }

  public Date randomDate(int offset) {
    Random rand = new Random();
    Date date = new Date();
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

  public String randomDateString(String format, int offset) {
    Date time = randomDate(offset);
    SimpleDateFormat timeFormat = new SimpleDateFormat(format);
    return timeFormat.format(time);
  }

  public String timestamp() {
    Date date = new Date();
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    return timeFormat.format(date);
  }

  public String randomListElement(List<String> values) {
    return values.get(randomNumber(0, values.size() - 1));
  }

  public String randomFirstName() {
    int index = randomNumber(1, (int) DatabaseManager.nameCount);
    NameRecord name = databaseManager.getNameById(index);
    return name.first;
  }

  public String randomLastName() {
    int index = randomNumber(1, (int) DatabaseManager.nameCount);
    NameRecord name = databaseManager.getNameById(index);
    return name.last;
  }

  public String randomFullName() {
    int index = randomNumber(1, (int) DatabaseManager.nameCount);
    NameRecord name = databaseManager.getNameById(index);
    return name.fullName();
  }

  public NameRecord randomNameRecord() {
    int firstIndex = randomNumber(1, (int) DatabaseManager.nameCount);
    int lastIndex = randomNumber(1, (int) DatabaseManager.nameCount);
    NameRecord firstName = databaseManager.getNameById(firstIndex);
    NameRecord lastName = databaseManager.getNameById(lastIndex);
    return new NameRecord(
        firstName.first,
        lastName.last,
        firstName.gender
    );
  }

  public String randomState() {
    double weight = randomDouble(0, 1, 4);
    return databaseManager.getState(weight);
  }

  public StateRecord randomStateRecord() {
    String state = randomState();
    List<StateRecord> records = databaseManager.getStateRecordsByState(state);
    int index = randomNumber(0, records.size() - 1);
    return records.get(index);
  }

  public AddressRecord randomAddressRecord() {
    int streetIndex = randomNumber(1, (int) DatabaseManager.addressCount);
    StateRecord stateRecord = randomStateRecord();
    String street = databaseManager.getStreetNameById(streetIndex);
    int number = randomNumber(100, 99999);
    return new AddressRecord(
        String.valueOf(number),
        street,
        stateRecord.city,
        stateRecord.state,
        stateRecord.zip
    );
  }

  public ProductRecord randomProductRecord() {
    int productIndex = randomNumber(1, (int) DatabaseManager.productCount);
    return databaseManager.getProductById(productIndex);
  }

  public String randomPhoneNumber(String state) {
    List<String> areaCodes = databaseManager.getAreaCodesByState(state);
    int number = randomNumber(1, 9999);
    int codeIndex = randomNumber(1, areaCodes.size());
    return areaCodes.get(codeIndex - 1) + "-555-" + String.format("%04d", number);
  }

  public boolean randomBoolean() {
    return rand.nextBoolean();
  }

  public String makeAlphaString(int minValue, int maxValue) {
    String numbers = "0123456789";
    String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lower = "abcdefghijklmnopqrstuvwxyz";
    String characters = upper + lower + numbers;
    char[] alphanum = characters.toCharArray();
    int length = randomNumber(minValue, maxValue);
    int max = alphanum.length - 1;

    StringBuilder string = new StringBuilder();
    for (int i = 0; i < length; i++) {
      string.append(alphanum[randomNumber(0, max)]);
    }

    return string.toString();
  }

  public String randomImagePath(boolean isNullable) {
    boolean isNull = isNullable && randomBoolean();
    if (isNull) {
      return "null";
    }
    return "/v1/" + randomNumber(1, 1000000) + "/200/300/image.jpg";
  }

  public String randomCreditCardNumber() {
    while (true) {
      String cardNumber = generateCardNumber();
      if (isValidLuhn(cardNumber)) {
        return cardNumber;
      }
    }
  }

  private String generateCardNumber() {
    String[] prefixes = {"4", "51", "52", "53", "54", "6011"};
    String prefix = prefixes[new Random().nextInt(prefixes.length)];

    StringBuilder cardNumber = new StringBuilder(prefix);

    while (cardNumber.length() < 15) {
      int digit = new Random().nextInt(10);
      cardNumber.append(digit);
    }

    cardNumber.append(calculateLuhnCheckDigit(cardNumber.toString()));

    return cardNumber.toString();
  }

  private int calculateLuhnCheckDigit(String number) {
    int sum = 0;
    boolean alternate = true;
    for (int i = number.length() - 1; i >= 0; i--) {
      int n = Character.getNumericValue(number.charAt(i));
      if (alternate) {
        n *= 2;
        if (n > 9) n -= 9;
      }
      sum += n;
      alternate = !alternate;
    }
    return (10 - sum % 10) % 10;
  }

  private static boolean isValidLuhn(String cardNumber) {
    int sum = 0;
    boolean alternate = false;
    for (int i = cardNumber.length() - 1; i >= 0; i--) {
      int n = Character.getNumericValue(cardNumber.charAt(i));
      if (alternate) {
        n *= 2;
        if (n > 9) n -= 9;
      }
      sum += n;
      alternate = !alternate;
    }
    return (sum % 10 == 0);
  }

  public String randomIpAddress() {
    Random random = new Random();
    int range = random.nextInt(3);

    return switch (range) {
      case 0 ->
          String.format("10.%d.%d.%d",
              random.nextInt(256),
              random.nextInt(256),
              random.nextInt(256));
      case 1 ->
          String.format("172.%d.%d.%d",
              16 + random.nextInt(16),
              random.nextInt(256),
              random.nextInt(256));
      default ->
          String.format("192.168.%d.%d",
              random.nextInt(256),
              random.nextInt(256));
    };
  }

  public int randomArrayIndex(int length) {
    int minValue = 0;
    int maxValue = length - 1;
    return randomNumber(minValue, maxValue);
  }

  public String loremWord() {
    return lorem[randomArrayIndex(lorem.length)];
  }

  public String punctuation() {
    return punctuation[randomArrayIndex(punctuation.length)];
  }

  public String terminator() {
    return terminator[randomArrayIndex(terminator.length)];
  }

  public String capitalizeFirst(String text) {
    return text.substring(0, 1).toUpperCase() + text.substring(1);
  }

  public String loremStart() {
    int minValue = 4;
    int maxValue = lorem.length;
    int limit = randomNumber(minValue, maxValue);
    String sentence = Arrays.stream(lorem)
        .limit(limit)
        .collect(Collectors.joining(" "));
    return capitalizeFirst(sentence) + terminator();
  }

  public String loremSegment() {
    List<String> list = Arrays.asList(lorem);
    Collections.shuffle(list);
    int minValue = 4;
    int maxValue = list.size();
    int limit = randomNumber(minValue, maxValue);
    return list.stream().limit(limit).collect(Collectors.joining(" "));
  }

  public String loremSentence() {
    if (randomNumber(0, 1) == 0) {
      return capitalizeFirst(loremSegment()) + terminator();
    }
    return capitalizeFirst(loremSegment()) + punctuation() + " " + loremSegment() + terminator();
  }

  public String loremText(int length) {
    StringBuilder s = new StringBuilder();
    s.append(loremStart());
    while (s.length() < length) {
      s.append(" ").append(loremSentence());
    }
    return s.toString();
  }
}
