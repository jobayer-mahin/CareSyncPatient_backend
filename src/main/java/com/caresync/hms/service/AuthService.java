package com.caresync.hms.service;

import com.caresync.hms.dto.LoginRequest;
import com.caresync.hms.dto.LoginResponse;
import com.caresync.hms.dto.RegisterRequest;
import com.caresync.hms.exception.InvalidCredentialsException;
import com.caresync.hms.model.User;
import com.caresync.hms.repository.UserRepository;
import com.caresync.hms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setMessage("Login successful");

        return response;
    }

    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate role
        String role = request.getRole().toUpperCase();
        if (!role.equals("PATIENT") && !role.equals("DOCTOR") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid role. Must be PATIENT or DOCTOR");
        }

        // This endpoint is public (permitAll in SecurityConfig), so it must never be
        // able to mint an ADMIN account -- otherwise anyone on the internet can grant
        // themselves full admin access with a single POST. ADMIN accounts must be
        // created by an existing authenticated ADMIN through a separate, protected
        // endpoint (not part of this Patient-module ZIP), never through self-service
        // registration.
        if (role.equals("ADMIN")) {
            throw new IllegalArgumentException("Public self-registration is not allowed for the ADMIN role");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);

        userRepository.save(user);

        return Map.of("message", "User registered successfully");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "isActive", user.getIsActive(),
                "lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : ""
        );
    }
}
