package com.example.apartmentsalesmanagementsystem.controller;

import com.example.apartmentsalesmanagementsystem.entity.AppointmentRequest;
import com.example.apartmentsalesmanagementsystem.entity.AppointmentRequestStatus;
import com.example.apartmentsalesmanagementsystem.entity.User;
import com.example.apartmentsalesmanagementsystem.entity.Apartment;
import com.example.apartmentsalesmanagementsystem.service.AppointmentRequestService;
import com.example.apartmentsalesmanagementsystem.service.UserService;
import com.example.apartmentsalesmanagementsystem.service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/appointment-requests")
public class AppointmentRequestController {
    
    @Autowired
    private AppointmentRequestService appointmentRequestService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ApartmentService apartmentService;
    
    // Client-side: Show appointment request form
    @GetMapping("/submit")
    public String showRequestForm(Model model) {
        List<Apartment> availableApartments = apartmentService.getAvailableApartments();
        model.addAttribute("appointmentRequest", new AppointmentRequest());
        model.addAttribute("availableApartments", availableApartments);
        return "appointment-requests/submit-request";
    }
    
    // Client-side: Submit appointment request
    @PostMapping("/submit")
    public String submitRequest(@ModelAttribute AppointmentRequest request,
                               @RequestParam(value = "apartmentId", required = false) Long apartmentId,
                               RedirectAttributes redirectAttributes) {
        try {
            // Set apartment if selected
            if (apartmentId != null) {
                Apartment apartment = apartmentService.findById(apartmentId).orElse(null);
                request.setApartment(apartment);
            }
            
            // Check if user is logged in
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !auth.getName().equals("anonymousUser")) {
                User currentUser = userService.findByUsername(auth.getName()).orElse(null);
                if (currentUser != null) {
                    request.setClient(currentUser);
                }
            }
            
            appointmentRequestService.createAppointmentRequest(request);
            redirectAttributes.addFlashAttribute("success", "Your appointment request has been submitted successfully! We will get back to you soon.");
            return "redirect:/appointment-requests/submit";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting request: " + e.getMessage());
            return "redirect:/appointment-requests/submit";
        }
    }
    
    // Client-side: View their requests (if logged in)
    @GetMapping("/my-requests")
    public String viewMyRequests(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName()).orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        List<AppointmentRequest> requests = appointmentRequestService.getRequestsByClient(currentUser);
        model.addAttribute("requests", requests);
        model.addAttribute("user", currentUser);
        return "appointment-requests/my-requests";
    }
    
    // Admin-side: View all appointment requests
    @GetMapping("/admin")
    public String adminViewAllRequests(Model model) {
        List<AppointmentRequest> requests = appointmentRequestService.getAllAppointmentRequests();
        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", appointmentRequestService.getTotalRequests());
        model.addAttribute("pendingRequests", appointmentRequestService.getPendingRequestsCount());
        model.addAttribute("approvedRequests", appointmentRequestService.getApprovedRequestsCount());
        model.addAttribute("rejectedRequests", appointmentRequestService.getRejectedRequestsCount());
        model.addAttribute("completedRequests", appointmentRequestService.getCompletedRequestsCount());
        return "admin/appointment-requests";
    }
    
    // Admin-side: View pending requests
    @GetMapping("/admin/pending")
    public String adminViewPendingRequests(Model model) {
        List<AppointmentRequest> requests = appointmentRequestService.getPendingRequests();
        model.addAttribute("requests", requests);
        model.addAttribute("title", "Pending Appointment Requests");
        return "admin/pending-requests";
    }
    
    // Admin-side: View request details
    @GetMapping("/admin/{id}")
    public String adminViewRequest(@PathVariable Long id, Model model) {
        AppointmentRequest request = appointmentRequestService.getAppointmentRequestById(id).orElse(null);
        
        if (request == null) {
            return "redirect:/appointment-requests/admin";
        }
        
        // Get available agents for assignment
        List<User> agents = userService.findUsersByManagementRole(com.example.apartmentsalesmanagementsystem.entity.ManagementRole.AGENT);
        
        model.addAttribute("request", request);
        model.addAttribute("agents", agents);
        model.addAttribute("requestStatuses", AppointmentRequestStatus.values());
        
        return "admin/request-details";
    }
    
    // Admin-side: Approve request
    @PostMapping("/admin/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                @RequestParam("adminReply") String adminReply,
                                @RequestParam(value = "assignedAgentId", required = false) Long assignedAgentId,
                                RedirectAttributes redirectAttributes) {
        try {
            User assignedAgent = null;
            if (assignedAgentId != null) {
                assignedAgent = userService.findById(assignedAgentId).orElse(null);
            }
            
            appointmentRequestService.approveRequest(id, adminReply, assignedAgent);
            redirectAttributes.addFlashAttribute("success", "Appointment request approved successfully!");
            return "redirect:/appointment-requests/admin/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving request: " + e.getMessage());
            return "redirect:/appointment-requests/admin/" + id;
        }
    }
    
    // Admin-side: Reject request
    @PostMapping("/admin/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                               @RequestParam("adminReply") String adminReply,
                               RedirectAttributes redirectAttributes) {
        try {
            appointmentRequestService.rejectRequest(id, adminReply);
            redirectAttributes.addFlashAttribute("success", "Appointment request rejected.");
            return "redirect:/appointment-requests/admin/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting request: " + e.getMessage());
            return "redirect:/appointment-requests/admin/" + id;
        }
    }
    
    // Admin-side: Complete request
    @PostMapping("/admin/{id}/complete")
    public String completeRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentRequestService.completeRequest(id);
            redirectAttributes.addFlashAttribute("success", "Appointment request marked as completed!");
            return "redirect:/appointment-requests/admin/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error completing request: " + e.getMessage());
            return "redirect:/appointment-requests/admin/" + id;
        }
    }
    
    // Admin-side: Add reply to request
    @PostMapping("/admin/{id}/reply")
    public String addReply(@PathVariable Long id,
                          @RequestParam("adminReply") String adminReply,
                          RedirectAttributes redirectAttributes) {
        try {
            appointmentRequestService.addAdminReply(id, adminReply);
            redirectAttributes.addFlashAttribute("success", "Reply added successfully!");
            return "redirect:/appointment-requests/admin/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding reply: " + e.getMessage());
            return "redirect:/appointment-requests/admin/" + id;
        }
    }
    
    // Admin-side: Assign agent to request
    @PostMapping("/admin/{id}/assign-agent")
    public String assignAgent(@PathVariable Long id,
                             @RequestParam("assignedAgentId") Long assignedAgentId,
                             RedirectAttributes redirectAttributes) {
        try {
            User agent = userService.findById(assignedAgentId).orElse(null);
            if (agent == null) {
                redirectAttributes.addFlashAttribute("error", "Selected agent not found");
                return "redirect:/appointment-requests/admin/" + id;
            }
            
            appointmentRequestService.assignAgent(id, agent);
            redirectAttributes.addFlashAttribute("success", "Agent assigned successfully!");
            return "redirect:/appointment-requests/admin/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error assigning agent: " + e.getMessage());
            return "redirect:/appointment-requests/admin/" + id;
        }
    }
    
    // Admin-side: Delete request
    @PostMapping("/admin/{id}/delete")
    public String deleteRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentRequestService.deleteAppointmentRequest(id);
            redirectAttributes.addFlashAttribute("success", "Appointment request deleted successfully!");
            return "redirect:/appointment-requests/admin";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting request: " + e.getMessage());
            return "redirect:/appointment-requests/admin/" + id;
        }
    }
}
