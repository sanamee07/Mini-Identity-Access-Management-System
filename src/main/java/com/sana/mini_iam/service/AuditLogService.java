package com.sana.mini_iam.service;


import com.sana.mini_iam.entity.AuditLog;
import com.sana.mini_iam.repository.AuditLogRepository;

import org.springframework.stereotype.Service;

import com.sana.mini_iam.dto.AuditLogResponse;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(
            String actorUsername,
            String action,
            String targetUsername,
            String details) {

        AuditLog auditLog = new AuditLog(
            actorUsername,
            action,
            targetUsername,
            details
        );

        auditLogRepository.save(auditLog);
    }

    public List<AuditLogResponse> getAllAuditLogs() {

    return auditLogRepository.findAllByOrderByTimestampDesc()
        .stream()
        .map(log -> new AuditLogResponse(
            log.getId(),
            log.getActorUsername(),
            log.getAction(),
            log.getTargetUsername(),
            log.getDetails(),
            log.getTimestamp()
        ))
        .toList();
}
}