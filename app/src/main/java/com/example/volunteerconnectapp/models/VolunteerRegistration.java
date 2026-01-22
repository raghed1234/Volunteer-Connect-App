package com.example.volunteerconnectapp.models;

public class VolunteerRegistration {
    private int registrationId;
    private int volunteerId;
    private int opportunityId;
    private String volunteerName;
    private String volunteerEmail;
    private String volunteerPhone;
    private String volunteerSkills;
    private String opportunityTitle;
    private String startDate;
    private String location;
    private String status;
    private String registeredAt;

    public VolunteerRegistration(int registrationId, int volunteerId, int opportunityId,
                                 String volunteerName, String volunteerEmail, String volunteerPhone,
                                 String volunteerSkills, String opportunityTitle, String startDate,
                                 String location, String status, String registeredAt) {
        this.registrationId = registrationId;
        this.volunteerId = volunteerId;
        this.opportunityId = opportunityId;
        this.volunteerName = volunteerName;
        this.volunteerEmail = volunteerEmail;
        this.volunteerPhone = volunteerPhone;
        this.volunteerSkills = volunteerSkills;
        this.opportunityTitle = opportunityTitle;
        this.startDate = startDate;
        this.location = location;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    // Getters
    public int getRegistrationId() { return registrationId; }
    public int getVolunteerId() { return volunteerId; }
    public int getOpportunityId() { return opportunityId; }
    public String getVolunteerName() { return volunteerName; }
    public String getVolunteerEmail() { return volunteerEmail; }
    public String getVolunteerPhone() { return volunteerPhone; }
    public String getVolunteerSkills() { return volunteerSkills; }
    public String getOpportunityTitle() { return opportunityTitle; }
    public String getStartDate() { return startDate; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getRegisteredAt() { return registeredAt; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}
