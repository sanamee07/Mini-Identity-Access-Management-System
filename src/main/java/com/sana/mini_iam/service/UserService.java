package com.sana.mini_iam.service;


import com.sana.mini_iam.entity.User;
import com.sana.mini_iam.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sana.mini_iam.entity.Role;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        AuditLogService auditLogService,
    PasswordEncoder passwordEncoder) {

    this.userRepository = userRepository;
    this.auditLogService = auditLogService;
    this.passwordEncoder = passwordEncoder;
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

@Transactional
public void changePassword(
        String username,
        String currentPassword,
        String newPassword) {

    // 1. Angemeldeten Benutzer aus der Datenbank laden
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            ));

    // 2. Bisheriges Passwort überprüfen
    if (!passwordEncoder.matches(
            currentPassword,
            user.getPassword())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Current password is incorrect"
        );
    }

    // 3. Prüfen, ob das neue Passwort dem alten entspricht
    if (passwordEncoder.matches(
            newPassword,
            user.getPassword())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "New password must be different from current password"
        );
    }

    // 4. Neues Passwort mit BCrypt hashen
    String hashedPassword = passwordEncoder.encode(newPassword);

    // 5. Passwort aktualisieren
    user.setPassword(hashedPassword);

    userRepository.save(user);

    // 6. Passwortänderung protokollieren
    auditLogService.logAction(
            username,
            "PASSWORD_CHANGED",
            username,
            "User changed their password"
    );
}





}