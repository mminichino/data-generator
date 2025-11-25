package com.codelry.util.generator.generator;

import com.codelry.util.generator.db.AddressRecord;
import com.codelry.util.generator.db.NameRecord;
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
    generateJson(value, gen, 0);
  }

  public static void generateJson(Table table, JsonGenerator gen, int index) throws IOException {
    NameRecord name = null;
    AddressRecord address = null;
    int digits = 5;
    boolean isDecimal = false;
    double value = 0;
    int number = 0;

    gen.writeStartObject();
    for (Column column : table.getColumns()) {
      gen.writeFieldName(column.name);
      switch (column.type) {
        case SEQUENTIAL_NUMBER:
          gen.writeNumber(table.getIndex());
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
      }
    }
  }
}
