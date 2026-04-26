package com.cfs.bms.controller;

import com.cfs.bms.dto.AuthRequest;
import com.cfs.bms.dto.AuthResponse;
import com.cfs.bms.dto.SignupRequest;
import com.cfs.bms.exception.ResourceNotFoundException;
import com.cfs.bms.model.User;
import com.cfs.bms.repository.UserRepository;
import com.cfs.bms.security.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String ADMIN_SECRET_CODE = "BMS_ADMIN_2024";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is already registered"));
        }

        String role = "ROLE_USER";
        if (ADMIN_SECRET_CODE.equals(request.getAdminCode())) {
            role = "ROLE_ADMIN";
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);

        User savedUser = userRepository.save(user);
        logger.info("New user registered: {} with role: {}", savedUser.getEmail(), role);

        String token = jwtUtil.generateToken(savedUser.getEmail(), role);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), role));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            logger.info("User logged in: {}", user.getEmail());

            return ResponseEntity.ok(
                    new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Not authenticated"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "role", user.getRole()
        ));
    }
}
