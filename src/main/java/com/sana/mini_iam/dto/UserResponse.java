package com.sana.mini_iam.dto;

import com.sana.mini_iam.entity.Role;

public record UserResponse(
    Long id,
    String username,
    String email,
    Role role,
    boolean enabled
) {
}