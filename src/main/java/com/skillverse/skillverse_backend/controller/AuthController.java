package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.User;
import com.skillverse.skillverse_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            User newUser = userService.signup(user.getEmail(), user.getName(), user.getPassword());
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Signup failed: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public ResponseEntity<?> loginSuccess() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Login successful");
            put("email", email);
        }});
    }

    @GetMapping("/failure")
    public ResponseEntity<?> loginFailure() {
        return ResponseEntity.status(401).body("Login failed");
    }

    @GetMapping("/logout-success")
    public ResponseEntity<?> logoutSuccess() {
        return ResponseEntity.ok("Logout successful");
    }
}