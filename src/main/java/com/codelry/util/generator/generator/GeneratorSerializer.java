package com.codelry.util.generator.generator;

import com.codelry.util.generator.db.AddressRecord;
import com.codelry.util.generator.db.NameRecord;
import com.codelry.util.generator.db.ProductRecord;
import com.codelry.util.generator.dto.Column;
import com.codelry.util.generator.dto.Table;
import com.codelry.util.generator.randomizer.Randomizer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class GeneratorSerializer extends JsonSerializer<Table> {
  private static final Logger LOGGER = LogManager.getLogger(GeneratorSerializer.class);
  private static final Randomizer randomizer = new Randomizer();

  @Override
  public void serialize(Table value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    generateJson(value, gen);
  }

  public static void generateJson(Table table, JsonGenerator gen) throws IOException {
    NameRecord name = null;
    AddressRecord address = null;
    ProductRecord product = null;
    int digits;
    boolean isDecimal;
    double value;
    int number;
    byte[] bytes;
    String[] list;

    gen.writeStartObject();
    for (Column column : table.getColumns()) {
      gen.writeFieldName(column.name);
      switch (column.type) {
        case SEQUENTIAL_NUMBER:
          gen.writeNumber(table.getIndex().getAndIncrement());
          break;
        case FIRST_NAME:
          name = (name == null) ? randomizer.randomNameRecord() : name;
          gen.writeString(name.first);
          break;
        case LAST_NAME:
          name = (name == null) ? randomizer.randomNameRecord() : name;
          gen.writeString(name.last);
          break;
        case FULL_NAME:
          name = (name == null) ? randomizer.randomNameRecord() : name;
          gen.writeString(name.fullName());
          break;
        case EMAIL:
          name = (name == null) ? randomizer.randomNameRecord() : name;
          gen.writeString(name.emailAddress());
          break;
        case STREET_ADDRESS:
          address = (address == null) ? randomizer.randomAddressRecord() : address;
          gen.writeString(address.number + " " + address.street);
          break;
        case CITY:
          address = (address == null) ? randomizer.randomAddressRecord() : address;
          gen.writeString(address.city);
          break;
        case STATE:
          address = (address == null) ? randomizer.randomAddressRecord() : address;
          gen.writeString(address.state);
          break;
        case ZIPCODE:
          address = (address == null) ? randomizer.randomAddressRecord() : address;
          gen.writeString(address.zip);
          break;
        case UUID:
          gen.writeString(randomizer.randomUuid());
          break;
        case CREDIT_CARD:
          gen.writeString(randomizer.randomCreditCardNumber());
          break;
        case PHONE_NUMBER:
          address = (address == null) ? randomizer.randomAddressRecord() : address;
          gen.writeString(randomizer.randomPhoneNumber(address.state));
          break;
        case BOOLEAN:
          gen.writeBoolean(randomizer.randomBoolean());
          break;
        case NUMBER:
          digits = column.options.containsKey("digits") ? Integer.parseInt(column.options.get("digits").toString()) : 5;
          isDecimal = column.options.containsKey("isDecimal") && Boolean.parseBoolean(column.options.get("isDecimal").toString());
          value = randomizer.randomNumber(digits, isDecimal);
          gen.writeNumber(isDecimal ? value : (long) value);
          break;
        case DOLLAR_AMOUNT:
          digits = column.options.containsKey("digits") ? Integer.parseInt(column.options.get("digits").toString()) : 4;
          value = randomizer.randomDollarAmount(digits);
          gen.writeNumber(value);
          break;
        case ACCOUNT_NUMBER:
          number = randomizer.randomNumber(1_000_000, 100_000_000);
          gen.writeString(String.format("%012d", number));
          break;
        case DATE:
          gen.writeString(randomizer.randomDateString("yyyy-MM-dd", -2));
          break;
        case TIMESTAMP:
          gen.writeString(randomizer.timestamp());
          break;
        case TEXT:
          gen.writeString(randomizer.loremText(25));
          break;
        case MAC_ADDRESS:
          bytes = new byte[6];
          randomizer.randomBytes(bytes);
          gen.writeString(String.format("%02X:%02X:%02X:%02X:%02X:%02X", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]));
          break;
        case IP_ADDRESS:
          gen.writeString(randomizer.randomIpAddress());
          break;
        case SET:
          list = column.options.containsKey("members") ? (String[]) column.options.get("members") : new String[]{"one", "two", "three"};
          gen.writeStartArray();
          for (String member : list) {
            gen.writeString(member);
          }
          gen.writeEndArray();
          break;
        case PRODUCT_NAME:
          product = (product == null) ? randomizer.randomProductRecord() : product;
          gen.writeString(product.name);
          break;
        case MANUFACTURER:
          product = (product == null) ? randomizer.randomProductRecord() : product;
          gen.writeString(product.manufacturer);
          break;
        case PRODUCT_TYPE:
          product = (product == null) ? randomizer.randomProductRecord() : product;
          gen.writeString(product.category);
          break;
        default:
          LOGGER.warn("Unknown column type: {}", column.type);
          gen.writeString("unknown");
          break;
      }
    }
    gen.writeEndObject();
  }
}
