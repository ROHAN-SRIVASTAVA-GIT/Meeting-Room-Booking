package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @GetMapping("/logout")
    public RedirectView logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        // Invalidate the session to clear user data
        session.invalidate();
        request.getSession().setAttribute("logoutMessage", "You have successfully logged out!");

        // Clear cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    cookie.setValue(null);
                    cookie.setPath("/"); // Set the path to the context root
                    cookie.setMaxAge(0); // Set cookie to expire immediately
                    response.addCookie(cookie);
                }
            }
        }

        // Set cache control headers to prevent caching
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        
        
        // Redirect to the login page
        return new RedirectView("/login?message=logout_success");
    }
}
