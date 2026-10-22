package com.sana.mini_iam.service;


import com.sana.mini_iam.dto.RegisterRequest;
import com.sana.mini_iam.entity.Role;
import com.sana.mini_iam.entity.User;
import com.sana.mini_iam.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sana.mini_iam.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;


@Service


public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager) {

    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "Email already exists"
            );
        }

        

        User user = new User();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(
            passwordEncoder.encode(request.password())
        );
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public void login(LoginRequest request) {

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()
        )
    );
}
    
}
