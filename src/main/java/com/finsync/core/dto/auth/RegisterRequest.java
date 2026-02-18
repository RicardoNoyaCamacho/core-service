package com.finsync.core.dto.auth;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}
