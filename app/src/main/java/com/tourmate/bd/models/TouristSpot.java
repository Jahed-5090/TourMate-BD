package com.tourmate.bd.models;

import java.util.List;

/**
 * Model representing a Tourist Spot within a District.
 * Contains comprehensive travel information to help tourists.
 */
public class TouristSpot {
    private String id;
    private String districtId;
    private String name;
    private String nameBn;
    private String shortDescription;
    private String fullDescription;

    // Travel Information
    private String howToReach;         // How to get there from Sylhet city
    private String transportOptions;   // Available transport modes
    private String approxCostFromDhaka;
    private String approxCostFromSylhet;
    private String entranceFee;
    private String bestTimeToVisit;
    private String openingHours;

    // Accommodation
    private String nearbyHotels;
    private String budgetAccommodation;

    // Food
    private String localFood;
    private String nearbyRestaurants;

    // Tips
    private String travelTips;
    private String warnings;           // Syndicate/scam warnings specific to this spot

    // Location
    private double latitude;
    private double longitude;
    private String googleMapsLink;

    // Media
    private int imageResId;            // Primary image
    private List<Integer> galleryImages; // Gallery images

    // Category
    private SpotCategory category;

    public enum SpotCategory {
        RELIGIOUS("Religious"),
        NATURAL("Natural"),
        HAOR("Haor & Wetland"),
        WATERFALL("Waterfall"),
        TEA_GARDEN("Tea Garden"),
        HISTORICAL("Historical"),
        ECO_PARK("Eco Park"),
        ARCHAEOLOGICAL("Archaeological"),
        FOREST("Forest");

        private final String displayName;
        SpotCategory(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public TouristSpot(String id, String districtId, String name, String nameBn,
                       String shortDescription, int imageResId, SpotCategory category) {
        this.id = id;
        this.districtId = districtId;
        this.name = name;
        this.nameBn = nameBn;
        this.shortDescription = shortDescription;
        this.imageResId = imageResId;
        this.category = category;
    }

    // ---- Getters ----
    public String getId() { return id; }
    public String getDistrictId() { return districtId; }
    public String getName() { return name; }
    public String getNameBn() { return nameBn; }
    public String getShortDescription() { return shortDescription; }
    public String getFullDescription() { return fullDescription; }
    public String getHowToReach() { return howToReach; }
    public String getTransportOptions() { return transportOptions; }
    public String getApproxCostFromDhaka() { return approxCostFromDhaka; }
    public String getApproxCostFromSylhet() { return approxCostFromSylhet; }
    public String getEntranceFee() { return entranceFee; }
    public String getBestTimeToVisit() { return bestTimeToVisit; }
    public String getOpeningHours() { return openingHours; }
    public String getNearbyHotels() { return nearbyHotels; }
    public String getBudgetAccommodation() { return budgetAccommodation; }
    public String getLocalFood() { return localFood; }
    public String getNearbyRestaurants() { return nearbyRestaurants; }
    public String getTravelTips() { return travelTips; }
    public String getWarnings() { return warnings; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getGoogleMapsLink() { return googleMapsLink; }
    public int getImageResId() { return imageResId; }
    public List<Integer> getGalleryImages() { return galleryImages; }
    public SpotCategory getCategory() { return category; }

    // ---- Setters ----
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }
    public void setHowToReach(String howToReach) { this.howToReach = howToReach; }
    public void setTransportOptions(String transportOptions) { this.transportOptions = transportOptions; }
    public void setApproxCostFromDhaka(String approxCostFromDhaka) { this.approxCostFromDhaka = approxCostFromDhaka; }
    public void setApproxCostFromSylhet(String approxCostFromSylhet) { this.approxCostFromSylhet = approxCostFromSylhet; }
    public void setEntranceFee(String entranceFee) { this.entranceFee = entranceFee; }
    public void setBestTimeToVisit(String bestTimeToVisit) { this.bestTimeToVisit = bestTimeToVisit; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public void setNearbyHotels(String nearbyHotels) { this.nearbyHotels = nearbyHotels; }
    public void setBudgetAccommodation(String budgetAccommodation) { this.budgetAccommodation = budgetAccommodation; }
    public void setLocalFood(String localFood) { this.localFood = localFood; }
    public void setNearbyRestaurants(String nearbyRestaurants) { this.nearbyRestaurants = nearbyRestaurants; }
    public void setTravelTips(String travelTips) { this.travelTips = travelTips; }
    public void setWarnings(String warnings) { this.warnings = warnings; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setGoogleMapsLink(String googleMapsLink) { this.googleMapsLink = googleMapsLink; }
    public void setGalleryImages(List<Integer> galleryImages) { this.galleryImages = galleryImages; }
}
