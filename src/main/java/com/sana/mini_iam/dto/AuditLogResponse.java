package com.sana.mini_iam.dto;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String actorUsername,
    String action,
    String targetUsername,
    String details,
    Instant timestamp
) {
}