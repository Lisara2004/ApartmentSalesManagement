package com.example.apartmentsalesmanagementsystem.service;

import com.example.apartmentsalesmanagementsystem.entity.AppointmentRequest;
import com.example.apartmentsalesmanagementsystem.entity.AppointmentRequestStatus;
import com.example.apartmentsalesmanagementsystem.entity.User;
import com.example.apartmentsalesmanagementsystem.entity.Apartment;
import com.example.apartmentsalesmanagementsystem.repository.AppointmentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentRequestService {
    
    @Autowired
    private AppointmentRequestRepository appointmentRequestRepository;
    
    // Create a new appointment request
    public AppointmentRequest createAppointmentRequest(AppointmentRequest request) {
        request.setStatus(AppointmentRequestStatus.PENDING);
        return appointmentRequestRepository.save(request);
    }
    
    // Get appointment request by ID
    public Optional<AppointmentRequest> getAppointmentRequestById(Long id) {
        return appointmentRequestRepository.findById(id);
    }
    
    // Get all appointment requests
    public List<AppointmentRequest> getAllAppointmentRequests() {
        return appointmentRequestRepository.findAll();
    }
    
    // Get requests by status
    public List<AppointmentRequest> getRequestsByStatus(AppointmentRequestStatus status) {
        return appointmentRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    // Get requests by client
    public List<AppointmentRequest> getRequestsByClient(User client) {
        return appointmentRequestRepository.findByClientOrderByCreatedAtDesc(client);
    }
    
    // Get requests by email
    public List<AppointmentRequest> getRequestsByEmail(String email) {
        return appointmentRequestRepository.findByEmailOrderByCreatedAtDesc(email);
    }
    
    // Get requests by assigned agent
    public List<AppointmentRequest> getRequestsByAssignedAgent(User agent) {
        return appointmentRequestRepository.findByAssignedAgentOrderByCreatedAtDesc(agent);
    }
    
    // Get requests by apartment
    public List<AppointmentRequest> getRequestsByApartment(Apartment apartment) {
        return appointmentRequestRepository.findByApartmentOrderByCreatedAtDesc(apartment);
    }
    
    // Get pending requests
    public List<AppointmentRequest> getPendingRequests() {
        return appointmentRequestRepository.findByStatusOrderByCreatedAtAsc(AppointmentRequestStatus.PENDING);
    }
    
    // Get requests with replies
    public List<AppointmentRequest> getRequestsWithReplies() {
        return appointmentRequestRepository.findRequestsWithReplies();
    }
    
    // Get requests without replies
    public List<AppointmentRequest> getRequestsWithoutReplies() {
        return appointmentRequestRepository.findRequestsWithoutReplies();
    }
    
    // Get recent requests (last 7 days)
    public List<AppointmentRequest> getRecentRequests() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return appointmentRequestRepository.findRecentRequests(sevenDaysAgo);
    }
    
    // Get requests that need attention (pending for more than 24 hours)
    public List<AppointmentRequest> getRequestsNeedingAttention() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return appointmentRequestRepository.findRequestsNeedingAttention(twentyFourHoursAgo);
    }
    
    // Update appointment request
    public AppointmentRequest updateAppointmentRequest(AppointmentRequest request) {
        return appointmentRequestRepository.save(request);
    }
    
    // Approve appointment request
    public AppointmentRequest approveRequest(Long requestId, String adminReply, User assignedAgent) {
        Optional<AppointmentRequest> requestOpt = appointmentRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment request not found");
        }
        
        AppointmentRequest request = requestOpt.get();
        if (request.getStatus() != AppointmentRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved");
        }
        
        request.setStatus(AppointmentRequestStatus.APPROVED);
        request.setAdminReply(adminReply);
        request.setAssignedAgent(assignedAgent);
        request.setRepliedAt(LocalDateTime.now());
        
        return appointmentRequestRepository.save(request);
    }
    
    // Reject appointment request
    public AppointmentRequest rejectRequest(Long requestId, String adminReply) {
        Optional<AppointmentRequest> requestOpt = appointmentRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment request not found");
        }
        
        AppointmentRequest request = requestOpt.get();
        if (request.getStatus() != AppointmentRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }
        
        request.setStatus(AppointmentRequestStatus.REJECTED);
        request.setAdminReply(adminReply);
        request.setRepliedAt(LocalDateTime.now());
        
        return appointmentRequestRepository.save(request);
    }
    
    // Mark request as completed
    public AppointmentRequest completeRequest(Long requestId) {
        Optional<AppointmentRequest> requestOpt = appointmentRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment request not found");
        }
        
        AppointmentRequest request = requestOpt.get();
        if (request.getStatus() != AppointmentRequestStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved requests can be marked as completed");
        }
        
        request.setStatus(AppointmentRequestStatus.COMPLETED);
        return appointmentRequestRepository.save(request);
    }
    
    // Add admin reply to request
    public AppointmentRequest addAdminReply(Long requestId, String adminReply) {
        Optional<AppointmentRequest> requestOpt = appointmentRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment request not found");
        }
        
        AppointmentRequest request = requestOpt.get();
        request.setAdminReply(adminReply);
        request.setRepliedAt(LocalDateTime.now());
        
        return appointmentRequestRepository.save(request);
    }
    
    // Assign agent to request
    public AppointmentRequest assignAgent(Long requestId, User agent) {
        Optional<AppointmentRequest> requestOpt = appointmentRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment request not found");
        }
        
        AppointmentRequest request = requestOpt.get();
        request.setAssignedAgent(agent);
        
        return appointmentRequestRepository.save(request);
    }
    
    // Delete appointment request
    public void deleteAppointmentRequest(Long requestId) {
        Optional<AppointmentRequest> requestOpt = appointmentRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment request not found");
        }
        
        AppointmentRequest request = requestOpt.get();
        if (request.getStatus() == AppointmentRequestStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed requests cannot be deleted");
        }
        
        appointmentRequestRepository.deleteById(requestId);
    }
    
    // Get statistics
    public long getTotalRequests() {
        return appointmentRequestRepository.count();
    }
    
    public long countRequestsByStatus(AppointmentRequestStatus status) {
        return appointmentRequestRepository.countByStatus(status);
    }
    
    public long countRequestsByClient(User client) {
        return appointmentRequestRepository.countByClient(client);
    }
    
    public long countRequestsByAssignedAgent(User agent) {
        return appointmentRequestRepository.countByAssignedAgent(agent);
    }
    
    public long countRequestsByApartment(Apartment apartment) {
        return appointmentRequestRepository.countByApartment(apartment);
    }
    
    // Get dashboard statistics
    public long getPendingRequestsCount() {
        return countRequestsByStatus(AppointmentRequestStatus.PENDING);
    }
    
    public long getApprovedRequestsCount() {
        return countRequestsByStatus(AppointmentRequestStatus.APPROVED);
    }
    
    public long getRejectedRequestsCount() {
        return countRequestsByStatus(AppointmentRequestStatus.REJECTED);
    }
    
    public long getCompletedRequestsCount() {
        return countRequestsByStatus(AppointmentRequestStatus.COMPLETED);
    }
}
