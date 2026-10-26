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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtectionService;

    //Audit-Logging
    private final AuditLogService auditLogService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditLogService auditLogService,
        BruteForceProtectionService bruteForceProtectionService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.bruteForceProtectionService = bruteForceProtectionService;

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

    // 1. Prüfen, ob zu viele fehlgeschlagene Versuche vorliegen
    if (bruteForceProtectionService.isBlocked(request.username())) {

    // Blockierten Anmeldeversuch protokollieren
    auditLogService.logAction(
        request.username(),
        "LOGIN_BLOCKED",
        request.username(),
        "Login blocked due to too many failed attempts"
    );

    throw new ResponseStatusException(
        HttpStatus.TOO_MANY_REQUESTS,
        "Too many login attempts. Please try again later."
    );
}

    try {

        // 2. Benutzername und Passwort überprüfen
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

    } catch (AuthenticationException exception) {

        // 3. Fehlgeschlagene Anmeldung protokollieren
        auditLogService.logAction(
            request.username(),
            "LOGIN_FAILED",
            request.username(),
            "Invalid credentials or disabled account"
        );

        throw exception;
    }

    // 4. JWT erstellen
    String token = jwtService.generateToken(request.username());

    // 5. Erfolgreiche Anmeldung protokollieren
    auditLogService.logAction(
        request.username(),
        "LOGIN_SUCCESS",
        request.username(),
        "Successful login"
    );

    // 6. JWT zurückgeben
    return token;
}
}