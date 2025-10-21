package com.example.apartmentsalesmanagementsystem.repository;

import com.example.apartmentsalesmanagementsystem.entity.Appointment;
import com.example.apartmentsalesmanagementsystem.entity.AppointmentStatus;
import com.example.apartmentsalesmanagementsystem.entity.User;
import com.example.apartmentsalesmanagementsystem.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find appointments by client
    List<Appointment> findByClientOrderByAppointmentDateTimeDesc(User client);
    
    // Find appointments by agent
    List<Appointment> findByAgentOrderByAppointmentDateTimeDesc(User agent);
    
    // Find appointments by apartment
    List<Appointment> findByApartmentOrderByAppointmentDateTimeDesc(Apartment apartment);
    
    // Find appointments by status
    List<Appointment> findByStatusOrderByAppointmentDateTimeDesc(AppointmentStatus status);
    
    // Find appointments by client and status
    List<Appointment> findByClientAndStatusOrderByAppointmentDateTimeDesc(User client, AppointmentStatus status);
    
    // Find appointments by agent and status
    List<Appointment> findByAgentAndStatusOrderByAppointmentDateTimeDesc(User agent, AppointmentStatus status);
    
    // Find appointments by apartment and status
    List<Appointment> findByApartmentAndStatusOrderByAppointmentDateTimeDesc(Apartment apartment, AppointmentStatus status);
    
    // Find appointments between two dates
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate ORDER BY a.appointmentDateTime")
    List<Appointment> findByAppointmentDateTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);
    
    // Find appointments for a specific date
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDateTime) = DATE(:date) ORDER BY a.appointmentDateTime")
    List<Appointment> findByAppointmentDate(@Param("date") LocalDateTime date);
    
    // Find upcoming appointments (future appointments)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime > :now AND a.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY a.appointmentDateTime")
    List<Appointment> findUpcomingAppointments(@Param("now") LocalDateTime now);
    
    // Find past appointments
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime < :now ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findPastAppointments(@Param("now") LocalDateTime now);
    
    // Find appointments by client and apartment
    List<Appointment> findByClientAndApartmentOrderByAppointmentDateTimeDesc(User client, Apartment apartment);
    
    // Find appointments by agent and apartment
    List<Appointment> findByAgentAndApartmentOrderByAppointmentDateTimeDesc(User agent, Apartment apartment);
    
    // Check if there's a conflict for an agent at a specific time
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.agent = :agent AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.appointmentDateTime BETWEEN :startTime AND :endTime")
    boolean existsConflictForAgent(@Param("agent") User agent, 
                                  @Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);
    
    // Count appointments by status
    long countByStatus(AppointmentStatus status);
    
    // Count appointments by client
    long countByClient(User client);
    
    // Count appointments by agent
    long countByAgent(User agent);
    
    // Count appointments by apartment
    long countByApartment(Apartment apartment);
    
    // Find appointments with feedback
    @Query("SELECT a FROM Appointment a WHERE a.feedbackRating IS NOT NULL ORDER BY a.feedbackRating DESC")
    List<Appointment> findAppointmentsWithFeedback();
    
    // Find appointments without feedback
    @Query("SELECT a FROM Appointment a WHERE a.feedbackRating IS NULL AND a.status = 'COMPLETED' ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findCompletedAppointmentsWithoutFeedback();
}
