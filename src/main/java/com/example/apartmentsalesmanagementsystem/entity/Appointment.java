package com.example.apartmentsalesmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment must be scheduled for a future date and time")
    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;
    
    @NotBlank(message = "Appointment notes are required")
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Column(name = "feedback_rating")
    private Integer feedbackRating; // 1-5 stars
    
    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Appointment() {}
    
    public Appointment(LocalDateTime appointmentDateTime, String notes, User client, User agent, Apartment apartment) {
        this.appointmentDateTime = appointmentDateTime;
        this.notes = notes;
        this.client = client;
        this.agent = agent;
        this.apartment = apartment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }
    
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    public User getClient() {
        return client;
    }
    
    public void setClient(User client) {
        this.client = client;
    }
    
    public User getAgent() {
        return agent;
    }
    
    public void setAgent(User agent) {
        this.agent = agent;
    }
    
    public Apartment getApartment() {
        return apartment;
    }
    
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public Integer getFeedbackRating() {
        return feedbackRating;
    }
    
    public void setFeedbackRating(Integer feedbackRating) {
        this.feedbackRating = feedbackRating;
    }
    
    public String getFeedbackComment() {
        return feedbackComment;
    }
    
    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }
    
    // Helper methods
    public LocalDate getAppointmentDate() {
        return appointmentDateTime.toLocalDate();
    }
    
    public String getFormattedDateTime() {
        return appointmentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
    }
    
    public String getFormattedDate() {
        return appointmentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
    
    public String getFormattedTime() {
        return appointmentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }
    
    public boolean isScheduled() {
        return status == AppointmentStatus.SCHEDULED;
    }
    
    public boolean isCompleted() {
        return status == AppointmentStatus.COMPLETED;
    }
    
    public boolean isCancelled() {
        return status == AppointmentStatus.CANCELLED;
    }
    
    public boolean isRescheduled() {
        return status == AppointmentStatus.RESCHEDULED;
    }
    
    public boolean canBeCancelled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.RESCHEDULED;
    }
    
    public boolean canBeRescheduled() {
        return status == AppointmentStatus.SCHEDULED;
    }
    
    public boolean hasFeedback() {
        return feedbackRating != null && feedbackRating > 0;
    }
}
