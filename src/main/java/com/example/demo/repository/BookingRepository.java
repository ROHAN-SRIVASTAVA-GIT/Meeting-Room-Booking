package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings for a specific room
    List<Booking> findAllByRoomName(String roomName);

    // Find all bookings ordered by bookingDateTime
    List<Booking> findAllByOrderByBookingDateTimeAsc();

    // Find bookings that overlap with a given dateTime range
    List<Booking> findByRoomNameAndBookingDateTimeBeforeAndEndDateTimeAfter(
        String roomName, LocalDateTime endDateTime, LocalDateTime startDateTime);
    
    // Custom delete method to delete booking by ID
    void deleteById(Long id);
    
    // Find bookings by room name and date range
    @Query("SELECT b FROM Booking b WHERE b.roomName = :roomName AND b.bookingDateTime >= :startDate AND b.endDateTime <= :endDate")
    List<Booking> findByRoomNameAndDateRange(@Param("roomName") String roomName, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    // Find bookings by date range only
    @Query("SELECT b FROM Booking b WHERE b.bookingDateTime >= :startDate AND b.endDateTime <= :endDate")
    List<Booking> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);

    // Find bookings by room name only
    List<Booking> findByRoomName(String roomName);
}
