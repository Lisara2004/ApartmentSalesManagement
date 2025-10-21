package com.example.apartmentsalesmanagementsystem.controller;

import com.example.apartmentsalesmanagementsystem.entity.Appointment;
import com.example.apartmentsalesmanagementsystem.entity.AppointmentStatus;
import com.example.apartmentsalesmanagementsystem.entity.User;
import com.example.apartmentsalesmanagementsystem.entity.Apartment;
import com.example.apartmentsalesmanagementsystem.service.AppointmentService;
import com.example.apartmentsalesmanagementsystem.service.UserService;
import com.example.apartmentsalesmanagementsystem.service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ApartmentService apartmentService;
    
    // View all appointments (admin only)
    @GetMapping("/admin")
    public String adminViewAllAppointments(Model model) {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        model.addAttribute("totalAppointments", appointmentService.getTotalAppointments());
        model.addAttribute("scheduledAppointments", appointmentService.countAppointmentsByStatus(AppointmentStatus.SCHEDULED));
        model.addAttribute("confirmedAppointments", appointmentService.countAppointmentsByStatus(AppointmentStatus.CONFIRMED));
        model.addAttribute("completedAppointments", appointmentService.countAppointmentsByStatus(AppointmentStatus.COMPLETED));
        model.addAttribute("cancelledAppointments", appointmentService.countAppointmentsByStatus(AppointmentStatus.CANCELLED));
        return "admin/appointments";
    }
    
    // View appointments for current user (client or agent)
    @GetMapping("/my")
    public String viewMyAppointments(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName()).orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        List<Appointment> appointments;
        if (currentUser.isAdmin() || currentUser.hasManagementRole(com.example.apartmentsalesmanagementsystem.entity.ManagementRole.AGENT)) {
            // Agent/Admin view - show appointments they're handling
            appointments = appointmentService.getAppointmentsByAgent(currentUser);
        } else {
            // Client view - show their appointments
            appointments = appointmentService.getAppointmentsByClient(currentUser);
        }
        
        model.addAttribute("appointments", appointments);
        model.addAttribute("user", currentUser);
        return "appointments/my-appointments";
    }
    
    // Create appointment form
    @GetMapping("/create")
    public String createAppointmentForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName()).orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Get available agents
        List<User> agents = userService.findUsersByManagementRole(com.example.apartmentsalesmanagementsystem.entity.ManagementRole.AGENT);
        
        // Get available apartments
        List<Apartment> availableApartments = apartmentService.getAvailableApartments();
        
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("agents", agents);
        model.addAttribute("availableApartments", availableApartments);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());
        
        return "appointments/create-appointment";
    }
    
    // Create appointment form with pre-selected apartment
    @GetMapping("/create/{apartmentId}")
    public String createAppointmentFormWithApartment(@PathVariable Long apartmentId, Model model, RedirectAttributes redirectAttributes) {
        return "redirect:/appointments/create";
    }
    
    // Create appointment
    @PostMapping("/create")
    public String createAppointment(@ModelAttribute Appointment appointment,
                                  @RequestParam("apartmentId") Long apartmentId,
                                  @RequestParam("agentId") Long agentId,
                                  @RequestParam("appointmentDate") String appointmentDate,
                                  @RequestParam("appointmentTime") String appointmentTime,
                                  RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByUsername(auth.getName()).orElse(null);
            
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Parse date and time
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            LocalDate date = LocalDate.parse(appointmentDate, dateFormatter);
            LocalTime time = LocalTime.parse(appointmentTime, timeFormatter);
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            
            // Set appointment details
            appointment.setAppointmentDateTime(dateTime);
            appointment.setClient(currentUser);
            
            User agent = userService.findById(agentId).orElse(null);
            if (agent == null) {
                redirectAttributes.addFlashAttribute("error", "Selected agent not found");
                return "redirect:/appointments/create";
            }
            appointment.setAgent(agent);
            
            Apartment apartment = apartmentService.findById(apartmentId).orElse(null);
            if (apartment == null) {
                redirectAttributes.addFlashAttribute("error", "Selected apartment not found");
                return "redirect:/appointments/create";
            }
            appointment.setApartment(apartment);
            
            // Validate required fields
            if (appointment.getAgent() == null || appointment.getApartment() == null) {
                redirectAttributes.addFlashAttribute("error", "Please select both agent and apartment");
                return "redirect:/appointments/create";
            }
            
            if (appointment.getNotes() == null || appointment.getNotes().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Appointment notes are required");
                return "redirect:/appointments/create";
            }
            
            appointmentService.createAppointment(appointment);
            redirectAttributes.addFlashAttribute("success", "Appointment scheduled successfully!");
            return "redirect:/appointments/my";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating appointment: " + e.getMessage());
            return "redirect:/appointments/create";
        }
    }
    
    // View appointment details
    @GetMapping("/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id).orElse(null);
        
        if (appointment == null) {
            return "redirect:/appointments/my";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName()).orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Check if user has access to this appointment
        if (!currentUser.isAdmin() && 
            !currentUser.hasManagementRole(com.example.apartmentsalesmanagementsystem.entity.ManagementRole.AGENT) &&
            !appointment.getClient().getId().equals(currentUser.getId()) &&
            !appointment.getAgent().getId().equals(currentUser.getId())) {
            return "redirect:/appointments/my";
        }
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("currentUser", currentUser);
        return "appointments/view-appointment";
    }
    
    // Edit appointment form
    @GetMapping("/{id}/edit")
    public String editAppointmentForm(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id).orElse(null);
        
        if (appointment == null) {
            return "redirect:/appointments/my";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName()).orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Check if user can edit this appointment
        if (!currentUser.isAdmin() && 
            !currentUser.hasManagementRole(com.example.apartmentsalesmanagementsystem.entity.ManagementRole.AGENT) &&
            !appointment.getClient().getId().equals(currentUser.getId()) &&
            !appointment.getAgent().getId().equals(currentUser.getId())) {
            return "redirect:/appointments/my";
        }
        
        // Get available agents and apartments
        List<User> agents = userService.findUsersByManagementRole(com.example.apartmentsalesmanagementsystem.entity.ManagementRole.AGENT);
        List<Apartment> availableApartments = apartmentService.getAvailableApartments();
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("agents", agents);
        model.addAttribute("availableApartments", availableApartments);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());
        
        return "appointments/edit-appointment";
    }
    
    // Update appointment
    @PostMapping("/{id}/edit")
    public String updateAppointment(@PathVariable Long id,
                                   @ModelAttribute Appointment appointment,
                                   @RequestParam("apartmentId") Long apartmentId,
                                   @RequestParam("agentId") Long agentId,
                                   @RequestParam("appointmentDate") String appointmentDate,
                                   @RequestParam("appointmentTime") String appointmentTime,
                                   RedirectAttributes redirectAttributes) {
        try {
            Appointment existingAppointment = appointmentService.getAppointmentById(id).orElse(null);
            
            if (existingAppointment == null) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/appointments/my";
            }
            
            // Parse date and time
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            LocalDate date = LocalDate.parse(appointmentDate, dateFormatter);
            LocalTime time = LocalTime.parse(appointmentTime, timeFormatter);
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            
            // Update appointment details
            existingAppointment.setAppointmentDateTime(dateTime);
            
            User agent = userService.findById(agentId).orElse(null);
            if (agent == null) {
                redirectAttributes.addFlashAttribute("error", "Selected agent not found");
                return "redirect:/appointments/" + id + "/edit";
            }
            existingAppointment.setAgent(agent);
            
            Apartment apartment = apartmentService.findById(apartmentId).orElse(null);
            if (apartment == null) {
                redirectAttributes.addFlashAttribute("error", "Selected apartment not found");
                return "redirect:/appointments/" + id + "/edit";
            }
            existingAppointment.setApartment(apartment);
            
            existingAppointment.setNotes(appointment.getNotes());
            existingAppointment.setStatus(appointment.getStatus());
            
            appointmentService.updateAppointment(existingAppointment);
            redirectAttributes.addFlashAttribute("success", "Appointment updated successfully!");
            return "redirect:/appointments/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating appointment: " + e.getMessage());
            return "redirect:/appointments/" + id + "/edit";
        }
    }
    
    // Confirm appointment
    @PostMapping("/{id}/confirm")
    public String confirmAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.confirmAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error confirming appointment: " + e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }
    
    // Complete appointment
    @PostMapping("/{id}/complete")
    public String completeAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.completeAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment marked as completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error completing appointment: " + e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }
    
    // Cancel appointment
    @PostMapping("/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                   @RequestParam("cancellationReason") String cancellationReason,
                                   RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id, cancellationReason);
            redirectAttributes.addFlashAttribute("success", "Appointment cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling appointment: " + e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }
    
    // Reschedule appointment
    @PostMapping("/{id}/reschedule")
    public String rescheduleAppointment(@PathVariable Long id,
                                       @RequestParam("newDate") String newDate,
                                       @RequestParam("newTime") String newTime,
                                       RedirectAttributes redirectAttributes) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            LocalDate date = LocalDate.parse(newDate, dateFormatter);
            LocalTime time = LocalTime.parse(newTime, timeFormatter);
            LocalDateTime newDateTime = LocalDateTime.of(date, time);
            
            appointmentService.rescheduleAppointment(id, newDateTime);
            redirectAttributes.addFlashAttribute("success", "Appointment rescheduled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rescheduling appointment: " + e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }
    
    // Mark as no show
    @PostMapping("/{id}/no-show")
    public String markAsNoShow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.markAsNoShow(id);
            redirectAttributes.addFlashAttribute("success", "Appointment marked as no show!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error marking appointment as no show: " + e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }
    
    // Add feedback
    @PostMapping("/{id}/feedback")
    public String addFeedback(@PathVariable Long id,
                             @RequestParam("rating") Integer rating,
                             @RequestParam("comment") String comment,
                             RedirectAttributes redirectAttributes) {
        try {
            appointmentService.addFeedback(id, rating, comment);
            redirectAttributes.addFlashAttribute("success", "Feedback submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting feedback: " + e.getMessage());
        }
        return "redirect:/appointments/" + id;
    }
    
    // Delete appointment
    @PostMapping("/{id}/delete")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.deleteAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment deleted successfully!");
            return "redirect:/appointments/my";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting appointment: " + e.getMessage());
            return "redirect:/appointments/" + id;
        }
    }
    
    // Calendar view
    @GetMapping("/calendar")
    public String calendarView(@RequestParam(value = "date", required = false) String dateStr, Model model) {
        LocalDate selectedDate = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        
        List<Appointment> appointmentsForDate = appointmentService.getAppointmentsForDate(selectedDate);
        
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("appointments", appointmentsForDate);
        
        return "appointments/calendar";
    }
}
