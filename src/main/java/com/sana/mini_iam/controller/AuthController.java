package com.sana.mini_iam.controller;

import com.sana.mini_iam.dto.RegisterRequest;
import com.sana.mini_iam.dto.LoginRequest;
import com.sana.mini_iam.dto.LoginResponse;

import com.sana.mini_iam.service.AuthService;
import com.sana.mini_iam.service.IpRateLimitService;
import com.sana.mini_iam.service.AuditLogService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final IpRateLimitService ipRateLimitService;
    private final AuditLogService auditLogService;

    public AuthController(
            AuthService authService,
            IpRateLimitService ipRateLimitService,
            AuditLogService auditLogService) {

        this.authService = authService;
        this.ipRateLimitService = ipRateLimitService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 1. IP-Adresse des Clients ermitteln
        String ipAddress = httpRequest.getRemoteAddr();

        // 2. IP-basiertes Rate-Limit überprüfen
        if (!ipRateLimitService.isAllowed(ipAddress)) {

            // Blockierte Anfrage protokollieren
            auditLogService.logAction(
                "anonymous",
                "IP_RATE_LIMIT_BLOCKED",
                request.username(),
                "Login rate limit exceeded"
            );

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();
        }

        // 3. Normale Anmeldung durchführen
        String token = authService.login(request);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}