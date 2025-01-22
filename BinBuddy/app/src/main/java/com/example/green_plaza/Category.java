package com.example.green_plaza;

public class Category {
    private String name;
    private int imageResId; // For local drawable resource ID
    private int id; // Unique ID for the category (optional)

    public Category(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
