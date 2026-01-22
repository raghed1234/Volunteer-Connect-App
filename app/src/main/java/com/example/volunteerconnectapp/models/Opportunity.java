package com.example.volunteerconnectapp.models;

public class Opportunity {
    private int id;
    private int orgId;
    private String title;
    private String description;
    private String location;
    private String startDate;
    private String endDate;
    private int capacity;
    private String status;
    private String imageUrl;
    private String organizationName;
    private String organizationLogo;
    private boolean isRegistered;

    // Constructor
    public Opportunity(int id, int orgId, String title, String description,
                       String location, String startDate, String endDate,
                       int capacity, String status, String imageUrl,
                       String organizationName, String organizationLogo, boolean isRegistered) {
        this.id = id;
        this.orgId = orgId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.status = status;
        this.imageUrl = imageUrl;
        this.organizationName = organizationName;
        this.organizationLogo = organizationLogo;
        this.isRegistered = isRegistered;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOrganizationLogo() { return organizationLogo; }
    public void setOrganizationLogo(String organizationLogo) { this.organizationLogo = organizationLogo; }

    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }
}