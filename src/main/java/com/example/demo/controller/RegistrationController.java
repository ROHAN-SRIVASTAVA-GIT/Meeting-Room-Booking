package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, User> userPending = new HashMap<>(); // To store pending users

    @GetMapping("/register")
    public String showRegistrationPage(Model model, HttpSession session, HttpServletResponse response) {
    	 // Prevent caching of the dashboard page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies

        return "register"; // renders the register.html
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes, HttpSession session, HttpServletResponse response) {
    	 // Prevent caching of the dashboard page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies


        // Validate if user already exists
        if (userService.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("errorRegister", "Username already exists.");
            return "redirect:/register";
        }
        if (userService.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("errorRegister", "Email already exists.");
            return "redirect:/register";
        }

        // Generate OTP
        String otp = generateOTP();
        otpStore.put(email, otp);
        sendEmail(email, otp);

        // Store pending user details
        userPending.put(email, new User(username, email, password));

        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("otpSent", true);

        return "redirect:/verify";
    }

    @GetMapping("/verify")
    public String showVerificationPage(Model model, HttpSession session, HttpServletResponse response) {
    	 // Prevent caching of the dashboard page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies

        return "verify"; // renders verify.html
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes, HttpSession session, HttpServletResponse response) {
    	 // Prevent caching of the dashboard page
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies

        String storedOtp = otpStore.get(email);

        if (storedOtp != null && storedOtp.equals(otp)) {
            // OTP is correct, save user to database
            User user = userPending.get(email);
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("successRegister", "Registration successful! You can now log in.");
            otpStore.remove(email); // clear OTP after successful registration
            userPending.remove(email); // clear pending user
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorVerify", "Invalid OTP. Please try again.");
            return "redirect:/verify";
        }
        
        
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp);
        mailSender.send(message);
    }
}
