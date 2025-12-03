package com.codelry.util.generator.generator;

import com.codelry.util.generator.db.AddressRecord;
import com.codelry.util.generator.db.NameRecord;
import com.codelry.util.generator.db.ProductRecord;
import com.codelry.util.generator.dto.*;
import com.codelry.util.generator.randomizer.Randomizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FieldGenerator {
  private static final Logger LOGGER = LogManager.getLogger(FieldGenerator.class);
  private static final Randomizer randomizer = new Randomizer();
  private NameRecord name;
  private AddressRecord address;
  private ProductRecord product;

  public void init() {
    this.name = randomizer.randomNameRecord();
    this.address = randomizer.randomAddressRecord();
    this.product = randomizer.randomProductRecord();
  }

  public Field generate(FieldDefinition definition, long index) {
    Field field = new Field();

    field.setName(definition.getName());
    field.setDataType(definition.getDataType());
    switch (definition.getType()) {
      case SEQUENTIAL_NUMBER:
        field.setValue(index);
        break;
      case FIRST_NAME:
        field.setValue(name.first);
        break;
      case LAST_NAME:
        field.setValue(name.last);
        break;
      case FULL_NAME:
        field.setValue(name.fullName());
        break;
      case EMAIL:
        field.setValue(name.emailAddress());
        break;
      case STREET_ADDRESS:
        field.setValue(address.number + " " + address.street);
        break;
      case CITY:
        field.setValue(address.city);
        break;
      case STATE:
        field.setValue(address.state);
        break;
      case ZIPCODE:
        field.setValue(address.zip);
        break;
      case UUID:
        field.setValue(randomizer.randomUuid());
        break;
      case CREDIT_CARD:
        field.setValue(randomizer.randomCreditCardNumber());
        break;
      case PHONE_NUMBER:
        field.setValue(randomizer.randomPhoneNumber(address.state));
        break;
      case BOOLEAN:
        field.setValue(randomizer.randomBoolean());
        break;
      case NUMBER:
        int digits = definition.options.containsKey("digits") ? Integer.parseInt(definition.options.get("digits").toString()) : 5;
        double value = randomizer.randomNumber(digits, false);
        field.setValue((long) value);
        break;
      case DOLLAR_AMOUNT:
        double amount = randomizer.randomDollarAmount(4);
        field.setValue(amount);
        break;
      case ACCOUNT_NUMBER:
        int number = randomizer.randomNumber(1_000_000, 100_000_000);
        field.setValue(String.format("%012d", number));
        break;
      case DATE:
        field.setValue(randomizer.randomDate(-2));
        break;
      case TIMESTAMP:
        field.setValue(randomizer.dateNow());
        break;
      case TEXT:
        field.setValue(randomizer.loremText(25));
        break;
      case MAC_ADDRESS:
        byte[] bytes = new byte[6];
        randomizer.randomBytes(bytes);
        field.setValue(String.format("%02X:%02X:%02X:%02X:%02X:%02X", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]));
        break;
      case IP_ADDRESS:
        field.setValue(randomizer.randomIpAddress());
        break;
      case SET:
        String[] list = definition.options.containsKey("members") ? (String[]) definition.options.get("members") : new String[]{"one", "two", "three"};
        field.setValue(list);
        break;
      case PRODUCT_NAME:
        field.setValue(product.name);
        break;
      case MANUFACTURER:
        field.setValue(product.manufacturer);
        break;
      case PRODUCT_TYPE:
        field.setValue(product.category);
        break;
      default:
        LOGGER.warn("Unknown column type: {}", definition.getType());
        field.setValue("unknown");
        break;
    }
    return field;
  }
}
