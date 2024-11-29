package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings for a specific room
    List<Booking> findAllByRoomName(String roomName);

    // Find all bookings ordered by bookingDateTime
    List<Booking> findAllByOrderByBookingDateTimeAsc();

    // Enhanced method to find overlapping bookings with more robust checking
    @Query("SELECT b FROM Booking b WHERE b.roomName = :roomName AND " +
           "((:startDateTime BETWEEN b.bookingDateTime AND b.endDateTime) OR " +
           "(:endDateTime BETWEEN b.bookingDateTime AND b.endDateTime) OR " +
           "(b.bookingDateTime BETWEEN :startDateTime AND :endDateTime))")
    List<Booking> findOverlappingBookings(
        @Param("roomName") String roomName,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    // Find bookings by room name and date range with more flexible querying
    @Query("SELECT b FROM Booking b WHERE " +
           "(:roomName IS NULL OR b.roomName = :roomName) AND " +
           "(:startDate IS NULL OR b.bookingDateTime >= :startDate) AND " +
           "(:endDate IS NULL OR b.endDateTime <= :endDate)")
    List<Booking> findFlexibleBookings(
        @Param("roomName") String roomName, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    // Soft delete method (consider adding a 'deleted' flag to your Booking entity)
    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.deleted = true WHERE b.id = :id")
    void softDeleteById(@Param("id") Long id);

    // Find bookings for a specific user
    List<Booking> findByBookedBy(String bookedBy);

    // Find upcoming bookings
    List<Booking> findByBookingDateTimeAfterOrderByBookingDateTimeAsc(LocalDateTime currentDateTime);

    // Complex overlap check method
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
           "b.roomName = :roomName AND " +
           "(:startDateTime < b.endDateTime AND :endDateTime > b.bookingDateTime)")
    boolean isRoomBooked(
        @Param("roomName") String roomName,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    // Optional: Find most recent booking
    Optional<Booking> findTopByRoomNameOrderByBookingDateTimeDesc(String roomName);
}
