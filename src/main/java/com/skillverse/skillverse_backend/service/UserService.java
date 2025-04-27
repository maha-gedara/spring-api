package com.skillverse.skillverse_backend.service;

import com.skillverse.skillverse_backend.model.User;
import com.skillverse.skillverse_backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Attempting to load user with email: " + email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
        System.out.println("User found: " + user.getEmail() + ", password: " + user.getPassword());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public User signup(String email, String name, String password) {
        System.out.println("Attempting to signup user: " + email);
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            System.out.println("Email already exists: " + email);
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        System.out.println("Creating user: " + email + ", encoded password: " + user.getPassword());
        return userRepository.save(user);
    }
}