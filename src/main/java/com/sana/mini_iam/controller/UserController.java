package com.sana.mini_iam.controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.sana.mini_iam.dto.ChangePasswordRequest;
import com.sana.mini_iam.service.UserService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Map<String, String> getMyProfile(
            Authentication authentication) {

        return Map.of(
            "username", authentication.getName()
        );
    }

    @PatchMapping("/me/password")
public ResponseEntity<String> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication authentication) {

    // Benutzername aus dem überprüften JWT-Kontext
    String username = authentication.getName();

    userService.changePassword(
            username,
            request.currentPassword(),
            request.newPassword()
    );

    return ResponseEntity.ok(
            "Password changed successfully"
    );
}
}