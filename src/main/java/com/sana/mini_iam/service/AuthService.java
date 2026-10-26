package com.sana.mini_iam.service;

import com.sana.mini_iam.dto.RegisterRequest;
import com.sana.mini_iam.dto.LoginRequest;
import com.sana.mini_iam.entity.Role;
import com.sana.mini_iam.entity.User;
import com.sana.mini_iam.repository.UserRepository;
import com.sana.mini_iam.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    //Audit-Logging
    private final AuditLogService auditLogService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;

        // NEU
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "Email already exists"
            );
        }

        User user = new User();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(
            passwordEncoder.encode(request.password())
        );
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        try {

            // 1. Benutzername und Passwort überprüfen
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
                )
            );

        } catch (AuthenticationException exception) {

            // 2. Fehlgeschlagene Anmeldung protokollieren
            auditLogService.logAction(
                request.username(),
                "LOGIN_FAILED",
                request.username(),
                "Invalid credentials or disabled account"
            );

            // Ursprüngliche Ausnahme weitergeben
            throw exception;
        }

        // 3. JWT erstellen
        String token = jwtService.generateToken(request.username());

        // 4. Erfolgreiche Anmeldung protokollieren
        auditLogService.logAction(
            request.username(),
            "LOGIN_SUCCESS",
            request.username(),
            "Successful login"
        );

        // 5. JWT zurückgeben
        return token;
    }
}