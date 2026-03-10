package com.finsync.core.controller;

import com.finsync.core.model.User;
import com.finsync.core.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        emailVerificationService.resendVerification(user.getUserId());
        return ResponseEntity.ok("Verification email sent");
    }
}
