package com.example.apartmentsalesmanagementsystem.repository;

import com.example.apartmentsalesmanagementsystem.entity.AppointmentRequest;
import com.example.apartmentsalesmanagementsystem.entity.AppointmentRequestStatus;
import com.example.apartmentsalesmanagementsystem.entity.User;
import com.example.apartmentsalesmanagementsystem.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, Long> {
    
    // Find requests by status
    List<AppointmentRequest> findByStatusOrderByCreatedAtDesc(AppointmentRequestStatus status);
    
    // Find requests by client (User)
    List<AppointmentRequest> findByClientOrderByCreatedAtDesc(User client);
    
    // Find requests by email
    List<AppointmentRequest> findByEmailOrderByCreatedAtDesc(String email);
    
    // Find requests by assigned agent
    List<AppointmentRequest> findByAssignedAgentOrderByCreatedAtDesc(User assignedAgent);
    
    // Find requests by apartment
    List<AppointmentRequest> findByApartmentOrderByCreatedAtDesc(Apartment apartment);
    
    // Find pending requests
    List<AppointmentRequest> findByStatusOrderByCreatedAtAsc(AppointmentRequestStatus status);
    
    // Find requests with replies
    @Query("SELECT ar FROM AppointmentRequest ar WHERE ar.adminReply IS NOT NULL AND ar.adminReply != '' ORDER BY ar.repliedAt DESC")
    List<AppointmentRequest> findRequestsWithReplies();
    
    // Find requests without replies
    @Query("SELECT ar FROM AppointmentRequest ar WHERE ar.adminReply IS NULL OR ar.adminReply = '' ORDER BY ar.createdAt ASC")
    List<AppointmentRequest> findRequestsWithoutReplies();
    
    // Find requests by date range
    @Query("SELECT ar FROM AppointmentRequest ar WHERE ar.createdAt BETWEEN :startDate AND :endDate ORDER BY ar.createdAt DESC")
    List<AppointmentRequest> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Count requests by status
    long countByStatus(AppointmentRequestStatus status);
    
    // Count requests by client
    long countByClient(User client);
    
    // Count requests by assigned agent
    long countByAssignedAgent(User assignedAgent);
    
    // Count requests by apartment
    long countByApartment(Apartment apartment);
    
    // Find recent requests (last 7 days)
    @Query("SELECT ar FROM AppointmentRequest ar WHERE ar.createdAt >= :sevenDaysAgo ORDER BY ar.createdAt DESC")
    List<AppointmentRequest> findRecentRequests(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
    
    // Find requests that need attention (pending for more than 24 hours)
    @Query("SELECT ar FROM AppointmentRequest ar WHERE ar.status = 'PENDING' AND ar.createdAt < :twentyFourHoursAgo ORDER BY ar.createdAt ASC")
    List<AppointmentRequest> findRequestsNeedingAttention(@Param("twentyFourHoursAgo") LocalDateTime twentyFourHoursAgo);
}
