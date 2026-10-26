package com.sana.mini_iam.service;


import com.sana.mini_iam.entity.User;
import com.sana.mini_iam.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sana.mini_iam.entity.Role;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public UserService(
        UserRepository userRepository,
        AuditLogService auditLogService) {

    this.userRepository = userRepository;
    this.auditLogService = auditLogService;
}

    @Transactional
    public void disableUser(Long id, String currentAdminUsername) {

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));

        if (user.getRole() == com.sana.mini_iam.entity.Role.ADMIN) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Administrator accounts cannot be disabled"
            );
        }

        user.setEnabled(false);

        userRepository.save(user);

        auditLogService.logAction(
    currentAdminUsername,
    "USER_DISABLED",
    user.getUsername(),
    "Account disabled"
);
    }

    @Transactional
    public void enableUser(Long id, String currentAdminUsername) {

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));

        user.setEnabled(true);

        userRepository.save(user);

        auditLogService.logAction(
    currentAdminUsername,
    "USER_ENABLED",
    user.getUsername(),
    "Account enabled"
);
    }


    @Transactional
public void updateUserRole(
        Long id,
        Role newRole,
        String currentAdminUsername) {

    // 1. Überprüfen, ob eine Rolle angegeben wurde
    if (newRole == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Role is required"
        );
    }

    // 2. Benutzer aus PostgreSQL laden
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "User not found"
        ));

    // 3. Administrator darf eigene Rolle nicht ändern
    if (Objects.equals(user.getUsername(), currentAdminUsername)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Administrators cannot change their own role"
        );
    }

    Role oldRole = user.getRole();
    // 4. Rolle aktualisieren
    user.setRole(newRole);

    // 5. Änderung speichern
    userRepository.save(user);

    auditLogService.logAction(
    currentAdminUsername,
    "ROLE_CHANGED",
    user.getUsername(),
    oldRole.name() + " -> " + newRole.name()
);
}



}