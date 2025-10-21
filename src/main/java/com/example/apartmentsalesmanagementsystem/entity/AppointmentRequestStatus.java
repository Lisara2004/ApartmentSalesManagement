package com.example.apartmentsalesmanagementsystem.entity;

public enum AppointmentRequestStatus {
    PENDING("Pending Review", "badge bg-warning"),
    APPROVED("Approved", "badge bg-success"),
    REJECTED("Rejected", "badge bg-danger"),
    COMPLETED("Completed", "badge bg-info");
    
    private final String displayName;
    private final String cssClass;
    
    AppointmentRequestStatus(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
