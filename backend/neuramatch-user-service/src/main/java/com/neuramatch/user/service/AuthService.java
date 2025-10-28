package com.neuramatch.user.service;

import com.neuramatch.user.dto.AuthResponse;
import com.neuramatch.user.dto.LoginRequest;
import com.neuramatch.user.dto.RegisterRequest;
import com.neuramatch.user.entity.User;
import com.neuramatch.user.repository.UserRepository;
import com.neuramatch.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .companyName(request.getCompanyName())
                .jobTitle(request.getJobTitle())
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)  // For simplicity, auto-verify emails
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .message("Registration successful")
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("Account is temporarily locked. Please try again later.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementLoginAttempts();
            userRepository.save(user);
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if account is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        // Reset login attempts on successful login
        user.resetLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("Login successful for: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newToken = jwtTokenProvider.generateToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .message("Token refreshed successfully")
                .build();
    }
}
