package com.sana.mini_iam.service;

import com.sana.mini_iam.repository.AuditLogRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int WINDOW_MINUTES = 5;

    private final AuditLogRepository auditLogRepository;

    public BruteForceProtectionService(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    public boolean isBlocked(String username) {

    Instant fiveMinutesAgo = Instant.now()
            .minus(WINDOW_MINUTES, ChronoUnit.MINUTES);

    long failedAttempts =
            auditLogRepository
                .countByTargetUsernameAndActionAndTimestampAfter(
                    username,
                    "LOGIN_FAILED",
                    fiveMinutesAgo
                );

    System.out.println(
        "BruteForce Check | Username: " + username +
        " | Failed attempts: " + failedAttempts
    );

    return failedAttempts >= MAX_FAILED_ATTEMPTS;
}
}