package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import com.example.demo.exception.BookingValidationException;

@Service
@Transactional(readOnly = true)
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    // Business hours constants
    private static final LocalTime BUSINESS_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime BUSINESS_END_TIME = LocalTime.of(21, 0);

    /**
     * Save a new booking with comprehensive validation
     * @param booking Booking to be saved
     * @return Saved booking
     */
    @Transactional
    public Booking saveBooking(Booking booking) {
        // Validate booking before saving
        validateBookingTime(booking);
        
        // Check for overlapping bookings
        if (isOverlap(booking.getRoomName(), booking.getBookingDateTime(), booking.getEndDateTime())) {
            throw new BookingValidationException("Booking conflicts with existing bookings");
        }

        // Save and return the booking
        return bookingRepository.save(booking);
    }

    /**
     * Get available booking slots for a specific room and date
     * @param roomName Room name
     * @param date Date to check availability
     * @param duration Desired booking duration
     * @return List of available time slots
     */
    public List<LocalDateTime[]> getAvailableSlots(String roomName, LocalDate date, Duration duration) {
        List<LocalDateTime[]> availableSlots = new ArrayList<>();

        LocalDateTime startOfDay = date.atTime(BUSINESS_START_TIME);
        LocalDateTime endOfDay = date.atTime(BUSINESS_END_TIME);

        // Fetch bookings for the specific room and date
        List<Booking> roomBookings = filterBookingsByRoomAndDate(roomName, date);

        // Sort bookings by start time
        roomBookings.sort((b1, b2) -> b1.getBookingDateTime().compareTo(b2.getBookingDateTime()));

        LocalDateTime potentialStart = startOfDay;

        // Find available slots between existing bookings
        for (Booking booking : roomBookings) {
            if (potentialStart.plus(duration).isBefore(booking.getBookingDateTime())) {
                availableSlots.add(new LocalDateTime[] { potentialStart, potentialStart.plus(duration) });
            }
            potentialStart = booking.getEndDateTime();
        }

        // Check for final slot at the end of the day
        if (potentialStart.plus(duration).isBefore(endOfDay) || potentialStart.plus(duration).isEqual(endOfDay)) {
            availableSlots.add(new LocalDateTime[] { potentialStart, potentialStart.plus(duration) });
        }

        return availableSlots;
    }

    /**
     * Filter bookings by room and date
     * @param roomName Room name
     * @param date Date to filter
     * @return List of bookings
     */
    private List<Booking> filterBookingsByRoomAndDate(String roomName, LocalDate date) {
        return bookingRepository.findByRoomNameAndDateRange(
            roomName, 
            date.atStartOfDay(), 
            date.atTime(LocalTime.MAX)
        );
    }

    /**
     * Validate booking time constraints
     * @param booking Booking to validate
     */
    private void validateBookingTime(Booking booking) {
        LocalDateTime startDateTime = booking.getBookingDateTime();
        LocalDateTime endDateTime = booking.getEndDateTime();
        
        // Validate day of week
        DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new BookingValidationException("Bookings are not allowed on weekends");
        }

        // Validate business hours
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = endDateTime != null ? endDateTime.toLocalTime() : startTime;
        
        if (startTime.isBefore(BUSINESS_START_TIME) || endTime.isAfter(BUSINESS_END_TIME)) {
            throw new BookingValidationException("Bookings are only allowed between 9 AM and 9 PM");
        }
    }

    /**
     * Check for booking overlaps
     * @param roomName Room name
     * @param startDateTime Start date and time
     * @param endDateTime End date and time
     * @return true if overlap exists, false otherwise
     */
    public boolean isOverlap(String roomName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime end = Optional.ofNullable(endDateTime).orElse(startDateTime);
        
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
            roomName, startDateTime, end
        );
        
        return !overlappingBookings.isEmpty();
    }

    /**
     * Find booking by ID
     * @param id Booking ID
     * @return Booking or null if not found
     */
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Delete booking by ID
     * @param id Booking ID to delete
     */
    @Transactional
    public void deleteBookingById(Long id) {
        bookingRepository.deleteById(id);
    }

    /**
     * Retrieve all bookings
     * @return List of all bookings
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Filter bookings with flexible criteria
     * @param roomName Room name (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return Filtered list of bookings
     */
    public List<Booking> filterBookings(String roomName, LocalDate startDate, LocalDate endDate) {
        // Use flexible query method from repository
        return bookingRepository.findFlexibleBookings(
            roomName, 
            startDate != null ? startDate.atStartOfDay() : null, 
            endDate != null ? endDate.atTime(LocalTime.MAX) : null
        );
    }
}
