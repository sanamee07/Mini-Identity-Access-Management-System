package com.sana.mini_iam.entity;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String actorUsername;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 50)
    private String targetUsername;

    @Column(length = 100)
    private String details;

    @Column(nullable = false)
    private Instant timestamp;

    public AuditLog() {
    }

    public AuditLog(
            String actorUsername,
            String action,
            String targetUsername,
            String details) {

        this.actorUsername = actorUsername;
        this.action = action;
        this.targetUsername = targetUsername;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getAction() {
        return action;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public String getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}