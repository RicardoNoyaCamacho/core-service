package com.finsync.core.service;

import com.finsync.core.model.EmailVerificationToken;
import com.finsync.core.model.User;
import com.finsync.core.repository.EmailVerificationTokenRepository;
import com.finsync.core.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void sendVerificationEmail(User user) {
        tokenRepository.findByUser_UserId(user.getUserId())
                .ifPresent(existing -> tokenRepository.deleteByUser_UserId(user.getUserId()));

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        String verificationUrl = baseUrl + "/api/v1/auth/verify-email?token=" + token;
        Map<String, Object> variables = Map.of(
                "username", user.getUsername(),
                "verificationUrl", verificationUrl
        );

        emailService.sendHtmlEmail(user.getEmail(), "Verifica tu email - FinSync", "verification", variables);
        log.info("Verification email sent to user {}", user.getUserId());
    }

    @Transactional
    public boolean verifyEmail(String token) {
        return tokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.isExpired()) {
                        log.warn("Expired verification token used for user {}", verificationToken.getUser().getUserId());
                        return false;
                    }
                    User user = verificationToken.getUser();
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    tokenRepository.delete(verificationToken);
                    log.info("Email verified for user {}", user.getUserId());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void resendVerification(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Email is already verified");
        }

        sendVerificationEmail(user);
    }
}
