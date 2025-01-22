package com.example.green_plaza;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {

    private String itemID; // Add itemID
    private String itemName;
    private String owner;
    private String location;
    private String itemCategory;
    private String itemDescription;
    private double price;
    private double quantity;
    private String itemImage;
    private String status;
private String userID;
    public Item() {
        // Default constructor (required for Firebase Firestore)
    }
    public Item(String itemID, String category, String itemName, String itemDescription, String location, double price, double quantity, String itemImage, String status, String userID) {
        this.itemID = itemID;
        this.itemCategory = category;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.location = location;
        this.price = price;
        this.quantity = quantity;
        this.itemImage = itemImage;
        this.status = status;
        this.userID = userID;
        // Optionally handle itemComment
    }



    // Constructor
    public Item(String itemID, String itemName, String owner, String location, String itemDescription, double price, String itemImage) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.owner = owner;
        this.location = location;
        this.itemDescription = itemDescription;
        this.price = price;
        this.itemImage = itemImage;
    }
    public Item(String itemID, String category, String itemName, String description, double price, double quantity, String imageUri) {
        this.itemID = itemID;
        this.itemCategory = category;
        this.itemName = itemName;
        this.itemDescription = description;
        this.price = price;
        this.quantity = quantity;
        this.itemImage = imageUri;
    }
    public Item(String itemID, String category, String name, String description, String location, double price, double quantity, String imageUri) {
        this.itemID = itemID;
        this.itemCategory = category;
        this.itemName = name;
        this.itemDescription = description;
        this.location = location;
        this.price = price;
        this.quantity = quantity;
        this.itemImage = imageUri;
    }

    // Getters and setters
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    // Parcelable implementation
    protected Item(Parcel in) {
        itemID = in.readString();
        itemName = in.readString();
        owner = in.readString();
        location = in.readString();
        itemCategory = in.readString();
        itemDescription = in.readString();
        price = in.readDouble();
        quantity = in.readDouble();
        itemImage = in.readString();
        status = in.readString();
        userID = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemID);
        dest.writeString(itemName);
        dest.writeString(owner);
        dest.writeString(location);
        dest.writeString(itemCategory);
        dest.writeString(itemDescription);
        dest.writeDouble(price);
        dest.writeDouble(quantity);
        dest.writeString(itemImage);
        dest.writeString(status);
        dest.writeString(userID);
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }


}
