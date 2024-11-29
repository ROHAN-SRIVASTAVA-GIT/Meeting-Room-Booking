package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Case-insensitive search for user by username or email
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)")
    Optional<User> findByUsernameOrEmail(
        @Param("username") String username, 
        @Param("email") String email
    );

    // Case-insensitive search for user by email
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    // Case-insensitive search for user by username
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsername(@Param("username") String username);

    // Additional PostgreSQL-specific methods

    // Check if user exists by username or email (case-insensitive)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)")
    boolean existsByUsernameOrEmail(
        @Param("username") String username, 
        @Param("email") String email
    );

    // Count users by username or email (case-insensitive)
    @Query("SELECT COUNT(u) FROM User u WHERE LOWER(u.username) = LOWER(:username) OR LOWER(u.email) = LOWER(:email)")
    long countByUsernameOrEmail(
        @Param("username") String username, 
        @Param("email") String email
    );

    // Advanced authentication method
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)) " +
           "AND u.password = :password")
    Optional<User> findByCredentials(
        @Param("identifier") String identifier, 
        @Param("password") String password
    );

    // Find user by partial email or username match
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Optional<User> findByPartialEmailOrUsername(@Param("searchTerm") String searchTerm);
}
