package com.sana.mini_iam.controller;

import com.sana.mini_iam.dto.UserResponse;
import com.sana.mini_iam.repository.UserRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}