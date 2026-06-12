package com.codelry.util.generator.generator;

import com.codelry.util.generator.db.*;
import com.codelry.util.generator.dto.Column;
import com.codelry.util.generator.dto.Table;
import com.codelry.util.generator.randomizer.Randomizer;
import com.codelry.util.generator.util.ColumnOptions;
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
    AirportRecord airportOrig = null;
    AirportRecord airportDest = null;
    AirlineRecord airline = null;
    int digits;
    boolean isDecimal;
    double value;
    int number;
    byte[] bytes;
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
        case VALUE:
          Number fixedValue = ColumnOptions.getNumericValue(column.getOptions());
          if (fixedValue instanceof Double || fixedValue instanceof Float) {
            gen.writeNumber(fixedValue.doubleValue());
          } else {
            gen.writeNumber(fixedValue.longValue());
          }
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
          gen.writeStartArray();
          for (String member : ColumnOptions.getSetMembers(column.getOptions())) {
            gen.writeString(member);
          }
          gen.writeEndArray();
          break;
        case WORD:
          gen.writeString(ColumnOptions.getWordValue(column.getOptions()));
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
        case AIRPORT_ORIG_CODE:
          airportOrig = (airportOrig == null) ? randomizer.randomAirportRecord() : airportOrig;
          gen.writeString(airportOrig.code);
          break;
        case AIRPORT_ORIG_NAME:
          airportOrig = (airportOrig == null) ? randomizer.randomAirportRecord() : airportOrig;
          gen.writeString(airportOrig.name);
          break;
        case AIRPORT_ORIG_CITY:
          airportOrig = (airportOrig == null) ? randomizer.randomAirportRecord() : airportOrig;
          gen.writeString(airportOrig.city);
          break;
        case AIRPORT_DEST_CODE:
          airportDest = (airportDest == null) ? randomizer.randomAirportRecord() : airportDest;
          gen.writeString(airportDest.code);
          break;
        case AIRPORT_DEST_NAME:
          airportDest = (airportDest == null) ? randomizer.randomAirportRecord() : airportDest;
          gen.writeString(airportDest.name);
          break;
        case AIRPORT_DEST_CITY:
          airportDest = (airportDest == null) ? randomizer.randomAirportRecord() : airportDest;
          gen.writeString(airportDest.city);
          break;
        case AIRLINE_CODE:
          airline = (airline == null) ? randomizer.randomAirlineRecord() : airline;
          gen.writeString(airline.code);
          break;
        case AIRLINE_NAME:
          airline = (airline == null) ? randomizer.randomAirlineRecord() : airline;
          gen.writeString(airline.name);
          break;
        case BOOKING_CODE:
          gen.writeString(randomizer.randomBookingCode());
          break;
        case CABIN_CODE:
          gen.writeString(randomizer.randomCabinCode());
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
