package com.finsync.core.controller;

import com.finsync.core.dto.auth.AuthenticationRequest;
import com.finsync.core.dto.auth.AuthenticationResponse;
import com.finsync.core.dto.auth.RegisterRequest;
import com.finsync.core.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
            ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> closeAccount(Authentication authentication) {
        authenticationService.closeAccount(authentication.getName());
        return ResponseEntity.accepted().build();
    }
}
