package com.finsync.core.dto.auth;

public record AuthenticationRequest(
        String email,
        String password
) {
}
