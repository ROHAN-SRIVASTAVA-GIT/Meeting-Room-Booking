package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Method to check if user is logged in
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUsername") != null;
    }

    // Redirect to login if not logged in
    @GetMapping("/dashboard")
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

    @PostMapping("/bookRoom")
    public String bookRoom(@RequestParam String roomName, @RequestParam String bookingDateTime,
                           @RequestParam(required = false) String endDateTime, @RequestParam String purpose,
                           @RequestParam String bookedBy, RedirectAttributes redirectAttributes, HttpSession session) {

        if (!isLoggedIn(session)) {
            return "redirect:/login"; // Redirect to login page if not logged in
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Parse booking date and time
        LocalDateTime startDateTime = LocalDateTime.parse(bookingDateTime, formatter);
        LocalDateTime endDateTimeParsed = (endDateTime != null && !endDateTime.isEmpty()) 
                                          ? LocalDateTime.parse(endDateTime, formatter) 
                                          : null;

        // Validation checks
        if (startDateTime.isBefore(now)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking start time must be in the future.");
            return "redirect:/dashboard";
        }

        if (endDateTimeParsed != null && endDateTimeParsed.isBefore(startDateTime)) {
            redirectAttributes.addFlashAttribute("errorMessage", "End time must be after start time.");
            return "redirect:/dashboard";
        }

        // Check for overlapping bookings
        if (bookingService.isOverlap(roomName, startDateTime, endDateTimeParsed)) {
            redirectAttributes.addFlashAttribute("errorMessage", "The room is already booked for the selected time slot.");
            return "redirect:/dashboard";
        }

        // Create and save booking
        Booking booking = new Booking();
        booking.setRoomName(roomName);
        booking.setBookingDateTime(startDateTime);
        booking.setEndDateTime(endDateTimeParsed);
        booking.setPurpose(purpose);
        booking.setBookedBy(bookedBy);

        bookingService.saveBooking(booking);
        redirectAttributes.addFlashAttribute("successMessage", "Booking Successful!");

        return "redirect:/dashboard";
    }

    @PostMapping("/deleteBooking")
    public String deleteBooking(@RequestParam Long bookingId, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login"; // Redirect to login page
        }

        try {
            bookingService.deleteBookingById(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting booking: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/fetchBookings")
    public @ResponseBody List<Map<String, Object>> fetchBookings(HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new RuntimeException("User not logged in");
        }

        List<Booking> bookings = bookingService.fetchAllBookings();
        return bookings.stream().map(booking -> {
            Map<String, Object> event = new HashMap<>();
            event.put("roomName", booking.getRoomName()); // + " - " + booking.getPurpose());
            event.put("start", booking.getBookingDateTime().toString());
            event.put("end", booking.getEndDateTime() != null ? booking.getEndDateTime().toString() : booking.getBookingDateTime().toString());
            event.put("description", booking.getPurpose());
            event.put("bookedBy", booking.getBookedBy());
            return event;
        }).collect(Collectors.toList());
    }

    @PostMapping("/editBooking")
    public String editBooking(@RequestParam Long bookingId, Model model, HttpSession session, HttpServletResponse response) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
     // Prevent caching of the dashboard page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies

        Booking booking = bookingService.findById(bookingId);
        if (booking != null) {
            model.addAttribute("booking", booking);
            return "editBooking";
        } else {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/updateBooking")
    public String updateBooking(@RequestParam Long id, @RequestParam String roomName, @RequestParam String bookingDateTime,
                                @RequestParam(required = false) String endDateTime, @RequestParam String purpose,
                                @RequestParam String bookedBy, RedirectAttributes redirectAttributes, HttpSession session) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(bookingDateTime, formatter);
        LocalDateTime endDateTimeParsed = (endDateTime != null && !endDateTime.isEmpty())
                                          ? LocalDateTime.parse(endDateTime, formatter)
                                          : null;

        // Validation checks
        if (startDateTime.isBefore(now)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking start time must be in the future.");
            return "redirect:/dashboard";
        }

        if (endDateTimeParsed != null && endDateTimeParsed.isBefore(startDateTime)) {
            redirectAttributes.addFlashAttribute("errorMessage", "End time must be after start time.");
            return "redirect:/dashboard";
        }

        // Check for overlapping bookings
        if (bookingService.isOverlap(roomName, startDateTime, endDateTimeParsed)) {
            redirectAttributes.addFlashAttribute("errorMessage", "The room is already booked for the selected time slot.");
            return "redirect:/dashboard";
        }

        try {
            Booking booking = bookingService.findById(id);
            if (booking != null) {
                booking.setRoomName(roomName);
                booking.setBookingDateTime(startDateTime);
                booking.setEndDateTime(endDateTimeParsed);
                booking.setPurpose(purpose);
                booking.setBookedBy(bookedBy);

                bookingService.saveBooking(booking);
                redirectAttributes.addFlashAttribute("successMessage", "Booking updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/dashboard";
    }
    
    @GetMapping("/bookings/filter")
    public @ResponseBody List<Map<String, Object>> filterBookings(
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            throw new RuntimeException("User not logged in");
        }

        List<Booking> filteredBookings = bookingService.filterBookings(roomName, startDate, endDate);
        return filteredBookings.stream().map(booking -> {
            Map<String, Object> event = new HashMap<>();
            event.put("roomName", booking.getRoomName()); // Room name
            event.put("start", booking.getBookingDateTime().toString()); // Start date/time
            event.put("end", booking.getEndDateTime() != null ? booking.getEndDateTime().toString() : booking.getBookingDateTime().toString()); // End date/time
            event.put("description", booking.getPurpose()); // Purpose
            event.put("bookedBy", booking.getBookedBy()); // Booked by
            return event;
        }).collect(Collectors.toList());
    }



}
