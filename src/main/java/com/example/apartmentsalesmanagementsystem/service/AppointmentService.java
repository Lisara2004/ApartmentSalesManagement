package com.example.apartmentsalesmanagementsystem.service;

import com.example.apartmentsalesmanagementsystem.entity.Appointment;
import com.example.apartmentsalesmanagementsystem.entity.AppointmentStatus;
import com.example.apartmentsalesmanagementsystem.entity.User;
import com.example.apartmentsalesmanagementsystem.entity.Apartment;
import com.example.apartmentsalesmanagementsystem.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    // Create a new appointment
    public Appointment createAppointment(Appointment appointment) {
        // Validate appointment time (must be in the future)
        if (appointment.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be scheduled for a future date and time");
        }
        
        // Check for scheduling conflicts (30-minute buffer)
        LocalDateTime startTime = appointment.getAppointmentDateTime();
        LocalDateTime endTime = startTime.plusMinutes(30);
        
        if (appointmentRepository.existsConflictForAgent(appointment.getAgent(), startTime, endTime)) {
            throw new IllegalArgumentException("Agent has a scheduling conflict at this time");
        }
        
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }
    
    // Get appointment by ID
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }
    
    // Get all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    // Get appointments by client
    public List<Appointment> getAppointmentsByClient(User client) {
        return appointmentRepository.findByClientOrderByAppointmentDateTimeDesc(client);
    }
    
    // Get appointments by agent
    public List<Appointment> getAppointmentsByAgent(User agent) {
        return appointmentRepository.findByAgentOrderByAppointmentDateTimeDesc(agent);
    }
    
    // Get appointments by apartment
    public List<Appointment> getAppointmentsByApartment(Apartment apartment) {
        return appointmentRepository.findByApartmentOrderByAppointmentDateTimeDesc(apartment);
    }
    
    // Get appointments by status
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateTimeDesc(status);
    }
    
    // Get upcoming appointments
    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findUpcomingAppointments(LocalDateTime.now());
    }
    
    // Get past appointments
    public List<Appointment> getPastAppointments() {
        return appointmentRepository.findPastAppointments(LocalDateTime.now());
    }
    
    // Get appointments for a specific date
    public List<Appointment> getAppointmentsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return appointmentRepository.findByAppointmentDateTimeBetween(startOfDay, endOfDay);
    }
    
    // Update appointment
    public Appointment updateAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            throw new IllegalArgumentException("Appointment ID is required for update");
        }
        
        Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
        if (existingAppointment.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment existing = existingAppointment.get();
        
        // If changing appointment time, check for conflicts
        if (!existing.getAppointmentDateTime().equals(appointment.getAppointmentDateTime())) {
            LocalDateTime startTime = appointment.getAppointmentDateTime();
            LocalDateTime endTime = startTime.plusMinutes(30);
            
            if (appointmentRepository.existsConflictForAgent(appointment.getAgent(), startTime, endTime)) {
                throw new IllegalArgumentException("Agent has a scheduling conflict at this time");
            }
        }
        
        return appointmentRepository.save(appointment);
    }
    
    // Confirm appointment
    public Appointment confirmAppointment(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only scheduled appointments can be confirmed");
        }
        
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }
    
    // Complete appointment
    public Appointment completeAppointment(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only confirmed or scheduled appointments can be completed");
        }
        
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }
    
    // Cancel appointment
    public Appointment cancelAppointment(Long appointmentId, String cancellationReason) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (!appointment.canBeCancelled()) {
            throw new IllegalArgumentException("This appointment cannot be cancelled");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(cancellationReason);
        return appointmentRepository.save(appointment);
    }
    
    // Reschedule appointment
    public Appointment rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (!appointment.canBeRescheduled()) {
            throw new IllegalArgumentException("This appointment cannot be rescheduled");
        }
        
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New appointment time must be in the future");
        }
        
        // Check for conflicts
        LocalDateTime startTime = newDateTime;
        LocalDateTime endTime = startTime.plusMinutes(30);
        
        if (appointmentRepository.existsConflictForAgent(appointment.getAgent(), startTime, endTime)) {
            throw new IllegalArgumentException("Agent has a scheduling conflict at this time");
        }
        
        appointment.setAppointmentDateTime(newDateTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        return appointmentRepository.save(appointment);
    }
    
    // Mark as no show
    public Appointment markAsNoShow(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only confirmed or scheduled appointments can be marked as no show");
        }
        
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        return appointmentRepository.save(appointment);
    }
    
    // Add feedback to appointment
    public Appointment addFeedback(Long appointmentId, Integer rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Feedback can only be added to completed appointments");
        }
        
        appointment.setFeedbackRating(rating);
        appointment.setFeedbackComment(comment);
        return appointmentRepository.save(appointment);
    }
    
    // Delete appointment
    public void deleteAppointment(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found");
        }
        
        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Completed appointments cannot be deleted");
        }
        
        appointmentRepository.deleteById(appointmentId);
    }
    
    // Get appointment statistics
    public long getTotalAppointments() {
        return appointmentRepository.count();
    }
    
    public long countAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }
    
    public long countAppointmentsByClient(User client) {
        return appointmentRepository.countByClient(client);
    }
    
    public long countAppointmentsByAgent(User agent) {
        return appointmentRepository.countByAgent(agent);
    }
    
    public long countAppointmentsByApartment(Apartment apartment) {
        return appointmentRepository.countByApartment(apartment);
    }
    
    // Get appointments with feedback
    public List<Appointment> getAppointmentsWithFeedback() {
        return appointmentRepository.findAppointmentsWithFeedback();
    }
    
    // Get completed appointments without feedback
    public List<Appointment> getCompletedAppointmentsWithoutFeedback() {
        return appointmentRepository.findCompletedAppointmentsWithoutFeedback();
    }
}
