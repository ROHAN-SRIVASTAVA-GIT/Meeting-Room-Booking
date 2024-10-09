package com.example.demo.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String usernameOrEmail, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> user = userService.loginUser(usernameOrEmail, password);

        if (user.isPresent()) {
            // Set the username in session
            session.setAttribute("loggedInUsername", user.get().getUsername());
            return "redirect:/dashboard";
        } else {
            model.addAttribute("errorLogin", "Invalid username/email or password");
            return "login";
        }
    }
}
