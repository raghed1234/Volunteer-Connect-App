package com.example.volunteerconnectapp.models;

public class UserProfile {
    private int id;
    private String fullname;
    private String email;
    private String userType;
    private String createdAt;
    private String logo;

    // Volunteer-specific
    private String bio;
    private String skills;
    private String availability;
    private String phoneNumber;
    private int registrationCount;

    // Organization-specific
    private String organizationName;
    private String website;
    private String address;
    private int opportunitiesCount;

    // Constructor
    public UserProfile() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getRegistrationCount() { return registrationCount; }
    public void setRegistrationCount(int registrationCount) { this.registrationCount = registrationCount; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getOpportunitiesCount() { return opportunitiesCount; }
    public void setOpportunitiesCount(int opportunitiesCount) { this.opportunitiesCount = opportunitiesCount; }

    public String getDisplayName() {
        if (userType != null && userType.equals("organization") && organizationName != null && !organizationName.isEmpty()) {
            return organizationName;
        }
        return fullname;
    }
}
