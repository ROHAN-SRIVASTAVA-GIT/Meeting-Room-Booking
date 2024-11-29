package com.example.demo.service;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.exception.UserAuthenticationException;
import com.example.demo.exception.UserAlreadyExistsException;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authenticate user with username/email and password
     * @param identifier Username or email
     * @param rawPassword Raw password
     * @return Authenticated user
     * @throws UserAuthenticationException if authentication fails
     */
    public Optional<User> loginUser(String identifier, String rawPassword) {
        return userRepository.findByUsernameOrEmail(identifier, identifier)
            .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
            .or(() -> {
                throw new UserAuthenticationException("Invalid credentials");
            });
    }

    /**
     * Get user by username
     * @param username Username
     * @return Optional of User
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by username
     * @param username Username
     * @return Optional of User
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     * @param email Email
     * @return Optional of User
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Register a new user
     * @param user User to be registered
     * @return Saved user
     * @throws UserAlreadyExistsException if user already exists
     */
    @Transactional
    public User registerUser(User user) {
        // Check if user already exists
        if (userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
            throw new UserAlreadyExistsException("Username or email already exists");
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save and return the user
        return userRepository.save(user);
    }

    /**
     * Save or update user
     * @param user User to save
     * @return Saved user
     */
    @Transactional
    public User saveUser(User user) {
        // If user exists, update; otherwise, create new
        return userRepository.save(user);
    }

    /**
     * Update user password
     * @param username Username
     * @param oldPassword Old password
     * @param newPassword New password
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserAuthenticationException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UserAuthenticationException("Invalid current password");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Delete user by username
     * @param username Username
     */
    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserAuthenticationException("User not found"));
        
        userRepository.delete(user);
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Check if user exists
     * @param username Username
     * @param email Email
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username, String email) {
        return userRepository.existsByUsernameOrEmail(username, email);
    }
}
