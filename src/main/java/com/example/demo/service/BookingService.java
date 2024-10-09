package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public void saveBooking(Booking booking) {
        validateBookingTime(booking);
        bookingRepository.save(booking);
    }
    
 // Business hours: 9 AM - 9 PM
    private static final LocalTime BUSINESS_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime BUSINESS_END_TIME = LocalTime.of(21, 0);

   

    public List<LocalDateTime[]> getAvailableSlots(String roomName, LocalDate date, Duration duration) {
        List<LocalDateTime[]> availableSlots = new ArrayList<>();

        LocalDateTime startOfDay = date.atTime(BUSINESS_START_TIME);
        LocalDateTime endOfDay = date.atTime(BUSINESS_END_TIME);

        List<Booking> roomBookings = filterBookingsByRoomAndDate(roomName, date);

        roomBookings.sort((b1, b2) -> b1.getBookingDateTime().compareTo(b2.getBookingDateTime()));

        LocalDateTime potentialStart = startOfDay;

        for (Booking booking : roomBookings) {
            if (potentialStart.plus(duration).isBefore(booking.getBookingDateTime())) {
                availableSlots.add(new LocalDateTime[] { potentialStart, potentialStart.plus(duration) });
            }
            potentialStart = booking.getEndDateTime();
        }

        if (potentialStart.plus(duration).isBefore(endOfDay) || potentialStart.plus(duration).isEqual(endOfDay)) {
            availableSlots.add(new LocalDateTime[] { potentialStart, potentialStart.plus(duration) });
        }

        return availableSlots;
    }

    private List<Booking> filterBookingsByRoomAndDate(String roomName, LocalDate date) {
        return bookingRepository.findByRoomNameAndDateRange(roomName, date.atStartOfDay(), date.atTime(23, 59));
    }

  

    private void validateBookingTime(Booking booking) {
        LocalDateTime startDateTime = booking.getBookingDateTime();
        LocalDateTime endDateTime = booking.getEndDateTime();
        LocalTime bookingStartTime = startDateTime.toLocalTime();
        LocalTime bookingEndTime = endDateTime != null ? endDateTime.toLocalTime() : bookingStartTime;

        // Ensure the booking is between Monday to Friday
        DayOfWeek dayOfWeek = startDateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Bookings are not allowed on weekends (Saturday or Sunday).");
        }

        // Ensure the booking is between 9 AM and 9 PM
        LocalTime startAllowed = LocalTime.of(9, 0);
        LocalTime endAllowed = LocalTime.of(21, 0);
        if (bookingStartTime.isBefore(startAllowed) || bookingEndTime.isAfter(endAllowed)) {
            throw new IllegalArgumentException("Bookings are only allowed between 9 AM and 9 PM.");
        }
    }

    public boolean isOverlap(String roomName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime end = endDateTime != null ? endDateTime : startDateTime;

        // Find bookings that overlap with the given date-time range
        List<Booking> bookings = bookingRepository.findByRoomNameAndBookingDateTimeBeforeAndEndDateTimeAfter(roomName, end, startDateTime);
        return !bookings.isEmpty();
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public void deleteBookingById(Long id) {
        bookingRepository.deleteById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> fetchAllBookings() {
        return bookingRepository.findAll();
    }
    
    public List<Booking> filterBookings(String roomName, LocalDate startDate, LocalDate endDate) {
        if (roomName != null && startDate != null && endDate != null) {
            return bookingRepository.findByRoomNameAndDateRange(roomName, startDate.atStartOfDay(), endDate.atTime(23, 59));
        } else if (roomName != null) {
            return bookingRepository.findByRoomName(roomName);
        } else if (startDate != null && endDate != null) {
            return bookingRepository.findByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59));
        } else {
            return bookingRepository.findAll();
        }
    }

}
