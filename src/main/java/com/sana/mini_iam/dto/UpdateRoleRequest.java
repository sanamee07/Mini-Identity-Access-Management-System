package com.sana.mini_iam.dto;

import com.sana.mini_iam.entity.Role;

public record UpdateRoleRequest(
    Role role
) {
}