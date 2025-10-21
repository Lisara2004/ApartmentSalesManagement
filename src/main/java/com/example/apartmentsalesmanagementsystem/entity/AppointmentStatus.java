package com.example.apartmentsalesmanagementsystem.entity;

public enum AppointmentStatus {
    SCHEDULED("Scheduled", "badge bg-primary"),
    CONFIRMED("Confirmed", "badge bg-info"),
    COMPLETED("Completed", "badge bg-success"),
    CANCELLED("Cancelled", "badge bg-danger"),
    RESCHEDULED("Rescheduled", "badge bg-warning"),
    NO_SHOW("No Show", "badge bg-secondary");
    
    private final String displayName;
    private final String cssClass;
    
    AppointmentStatus(String displayName, String cssClass) {
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
