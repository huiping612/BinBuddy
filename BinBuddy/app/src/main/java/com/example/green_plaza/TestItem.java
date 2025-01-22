package com.example.green_plaza;

public class TestItem {
    private String itemCategory;
    private String itemName;
    private String itemDescription;
    private double price;
    private String itemImage;
    private String status;

    public TestItem(String itemCategory, String itemName, String itemDescription, double price, String itemImage, String status) {
        this.itemCategory = itemCategory;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.price = price;
        this.itemImage = itemImage;
        this.status = status;
    }

    // Getters and setters
    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
