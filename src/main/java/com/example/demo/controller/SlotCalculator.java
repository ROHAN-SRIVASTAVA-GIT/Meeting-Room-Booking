package com.example.demo.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class SlotCalculator {

    @Autowired
    private BookingService bookingService;
    
    // Method to check if user is logged in
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUsername") != null;
    }


    @GetMapping("/available-slots")
    
    public String showDashboard(HttpSession session, Model model, HttpServletResponse response) {
        if (!isLoggedIn(session)) {
            return "redirect:/login"; // Redirect to login page if not logged in
        }

        // Prevent caching of the dashboard page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies

        // Retrieve the username from the session
        String loggedInUsername = (String) session.getAttribute("loggedInUsername");
        model.addAttribute("username", loggedInUsername);

        // Retrieve and set the list of bookings
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);

        return "dashboard";
    }
    public String getBookings() {
        return "available-slots";
    }

    @PostMapping("/available-slots")
    public ResponseEntity<List<LocalDateTime[]>> getAvailableSlots(@RequestBody AvailableSlotsRequest request) {
        try {
            System.out.println("Received request: RoomName=" + request.getRoomName() +
                                ", Date=" + request.getDate() + 
                                ", Duration=" + request.getDuration());

            Duration duration = Duration.ofMinutes(request.getDuration());
            LocalDate date = LocalDate.parse(request.getDate());

            // Fetch available slots
            List<LocalDateTime[]> availableSlots = bookingService.getAvailableSlots(request.getRoomName(), date, duration);

            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            System.err.println("Error while fetching available slots: " + e.getMessage());
            e.printStackTrace(); // This will print the full stack trace for debugging
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/book")
    public ResponseEntity<String> bookSlot(@RequestBody Booking booking) {
        bookingService.saveBooking(booking);
        return ResponseEntity.ok("Booking confirmed.");
    }
}

class AvailableSlotsRequest {
    private String roomName;
    private String date;
    private long duration; // Duration in minutes

    // Getters and Setters
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}
