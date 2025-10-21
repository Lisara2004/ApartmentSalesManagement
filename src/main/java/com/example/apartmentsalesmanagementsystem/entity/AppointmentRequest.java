package com.example.apartmentsalesmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_requests")
public class AppointmentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Client name is required")
    @Column(name = "client_name", nullable = false)
    private String clientName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @NotBlank(message = "Preferred date is required")
    @Column(name = "preferred_date", nullable = false)
    private String preferredDate;
    
    @NotBlank(message = "Preferred time is required")
    @Column(name = "preferred_time", nullable = false)
    private String preferredTime;
    
    @NotBlank(message = "Message is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentRequestStatus status = AppointmentRequestStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;
    
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;
    
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
    public AppointmentRequest() {}
    
    public AppointmentRequest(String clientName, String email, String phoneNumber, 
                            String preferredDate, String preferredTime, String message) {
        this.clientName = clientName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.preferredDate = preferredDate;
        this.preferredTime = preferredTime;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getClientName() {
        return clientName;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getPreferredDate() {
        return preferredDate;
    }
    
    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }
    
    public String getPreferredTime() {
        return preferredTime;
    }
    
    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public AppointmentRequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentRequestStatus status) {
        this.status = status;
    }
    
    public Apartment getApartment() {
        return apartment;
    }
    
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
    
    public User getClient() {
        return client;
    }
    
    public void setClient(User client) {
        this.client = client;
    }
    
    public User getAssignedAgent() {
        return assignedAgent;
    }
    
    public void setAssignedAgent(User assignedAgent) {
        this.assignedAgent = assignedAgent;
    }
    
    public String getAdminReply() {
        return adminReply;
    }
    
    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
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
    
    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }
    
    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }
    
    // Helper methods
    public String getFormattedCreatedDate() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
    }
    
    public String getFormattedRepliedDate() {
        return repliedAt != null ? repliedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) : null;
    }
    
    public boolean isPending() {
        return status == AppointmentRequestStatus.PENDING;
    }
    
    public boolean isApproved() {
        return status == AppointmentRequestStatus.APPROVED;
    }
    
    public boolean isRejected() {
        return status == AppointmentRequestStatus.REJECTED;
    }
    
    public boolean isCompleted() {
        return status == AppointmentRequestStatus.COMPLETED;
    }
    
    public boolean hasReply() {
        return adminReply != null && !adminReply.trim().isEmpty();
    }
    
    public boolean isAssigned() {
        return assignedAgent != null;
    }
}
