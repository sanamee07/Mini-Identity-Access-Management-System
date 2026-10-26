package com.sana.mini_iam.controller;

import com.sana.mini_iam.dto.UserResponse;
import com.sana.mini_iam.repository.UserRepository;
import com.sana.mini_iam.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.sana.mini_iam.dto.UpdateRoleRequest;
import org.springframework.security.core.Authentication;

import com.sana.mini_iam.dto.AuditLogResponse;
import com.sana.mini_iam.service.AuditLogService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminController(
            UserRepository userRepository,
            UserService userService, 
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public Map<String, String> dashboard() {

        return Map.of(
            "message", "Welcome to the admin dashboard"
        );
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
            .stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
            ))
            .toList();
    }

   @PatchMapping("/users/{id}/disable")
public ResponseEntity<Void> disableUser(
        @PathVariable Long id,
        Authentication authentication) {

    userService.disableUser(
        id,
        authentication.getName()
    );

    return ResponseEntity.noContent().build();
}

    @PatchMapping("/users/{id}/enable")
public ResponseEntity<Void> enableUser(
        @PathVariable Long id,
        Authentication authentication) {

    userService.enableUser(
        id,
        authentication.getName()
    );

    return ResponseEntity.noContent().build();
}

    @PatchMapping("/users/{id}/role")
public ResponseEntity<Void> updateUserRole(
        @PathVariable Long id,
        @RequestBody UpdateRoleRequest request,
        Authentication authentication) {

    userService.updateUserRole(
        id,
        request.role(),
        authentication.getName()
    );

    return ResponseEntity.noContent().build();
}

@GetMapping("/audit-logs")
public List<AuditLogResponse> getAuditLogs() {

    return auditLogService.getAllAuditLogs();
}
}