package com.codelry.util.generator.db;

public class ProductRecord {
  public String department;
  public String manufacturer;
  public String category;
  public String subcategory;
  public String sku;
  public String name;
  public boolean seasonal;
  public float price;
  public float cost;

  public ProductRecord(String department, String manufacturer, String category, String subcategory, String sku, String name, boolean seasonal, float price, float cost) {
    this.department = department;
    this.manufacturer = manufacturer;
    this.category = category;
    this.subcategory = subcategory;
    this.sku = sku;
    this.name = name;
    this.seasonal = seasonal;
    this.price = price;
    this.cost = cost;
  }

  public String getDepartment() {
    return department;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public String getCategory() {
    return category;
  }

  public String getSubcategory() {
    return subcategory;
  }

  public String getSku() {
    return sku;
  }

  public String getName() {
    return name;
  }

  public boolean isSeasonal() {
    return seasonal;
  }

  public float getPrice() {
    return price;
  }

  public float getCost() {
    return cost;
  }
}
