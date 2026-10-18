package com.tourmate.bd.models;

import java.util.List;

/**
 * Model representing a District within a Division (e.g., Sylhet, Moulvibazar)
 */
public class District {
    private String id;
    private String divisionId;
    private String name;
    private String nameBn;
    private String description;
    private int imageResId;
    private List<TouristSpot> spots;

    public District(String id, String divisionId, String name, String nameBn,
                    String description, int imageResId) {
        this.id = id;
        this.divisionId = divisionId;
        this.name = name;
        this.nameBn = nameBn;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getId() { return id; }
    public String getDivisionId() { return divisionId; }
    public String getName() { return name; }
    public String getNameBn() { return nameBn; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public List<TouristSpot> getSpots() { return spots; }
    public void setSpots(List<TouristSpot> spots) { this.spots = spots; }
}
