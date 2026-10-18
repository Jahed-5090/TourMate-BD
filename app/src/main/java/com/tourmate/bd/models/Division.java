package com.tourmate.bd.models;

import java.util.List;

/**
 * Model representing a Division of Bangladesh (e.g., Sylhet, Dhaka)
 */
public class Division {
    private String id;
    private String name;          // English name
    private String nameBn;        // Bangla name
    private String description;
    private int imageResId;       // drawable resource
    private List<District> districts;

    public Division(String id, String name, String nameBn, String description, int imageResId) {
        this.id = id;
        this.name = name;
        this.nameBn = nameBn;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameBn() { return nameBn; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public List<District> getDistricts() { return districts; }
    public void setDistricts(List<District> districts) { this.districts = districts; }
}
