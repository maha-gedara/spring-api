package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.User;
import com.skillverse.skillverse_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        User user = userService.signup(request.getEmail(), request.getName(), request.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public ResponseEntity<?> loginSuccess(@AuthenticationPrincipal OidcUser principal) {
        if (principal == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No authenticated user found");
            error.put("detail", "Ensure OAuth login completed successfully and session is maintained");
            return ResponseEntity.status(401).body(error);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("email", principal.getEmail());
        response.put("name", principal.getFullName());
        response.put("idToken", principal.getIdToken().getTokenValue());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/failure")
    public ResponseEntity<?> loginFailure() {
        return ResponseEntity.status(401).body("Authentication failed");
    }
}

class SignupRequest {
    private String email;
    private String name;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}