package gr.aegean.service;

import gr.aegean.exception.BadCredentialsException;
import gr.aegean.repository.PasswordResetRepository;
import gr.aegean.repository.UserRepository;
import gr.aegean.security.password.PasswordResetConfirmationRequest;
import gr.aegean.security.password.PasswordResetRequest;
import gr.aegean.security.password.PasswordResetResult;
import gr.aegean.security.password.PasswordResetToken;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import gr.aegean.utility.PasswordValidation;
import gr.aegean.utility.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetResult createPasswordResetToken(PasswordResetRequest passwordResetRequest) {
        validatePasswordResetRequest(passwordResetRequest);

        userRepository.findUserByEmail(passwordResetRequest.email())
                .ifPresent(user -> {
                    String token = generateToken();
                    LocalDateTime expiryDate = LocalDateTime.now().plusHours(2);
                    String hashedToken = StringUtils.hashToken(token);
                    PasswordResetToken passwordResetToken = new PasswordResetToken(
                            user.getId(),
                            hashedToken,
                            expiryDate);
                    passwordResetRepository.createPasswordResetToken(passwordResetToken);

                    emailService.sendPasswordResetLinkEmail(passwordResetRequest.email(), token);
                });

        return new PasswordResetResult("If your email address exists in our database, you will receive a password " +
                "recovery link at your email address in a few minutes.");
    }

    public void validatePasswordResetToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new BadCredentialsException("Reset password token is invalid");
        }

        String hashedToken = StringUtils.hashToken(token);
        passwordResetRepository.findPasswordResetToken(hashedToken)
                .ifPresentOrElse(passwordResetToken -> {
                    if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        throw new BadCredentialsException("The password reset link has expired. " +
                                "Please request a new one.");
                    }}, () -> {
                    throw new BadCredentialsException("Reset password token is invalid");
                });
    }

    public void resetPassword(PasswordResetConfirmationRequest resetConfirmationRequest) {
        //validate the token
        validatePasswordResetToken(resetConfirmationRequest.token());

        //validate/hash the updated password
        PasswordValidation.validatePassword(resetConfirmationRequest.updatedPassword());
        String hashedPassword = passwordEncoder.encode(resetConfirmationRequest.updatedPassword());

        //update the password in db and delete the password reset token record after
        String hashedToken = StringUtils.hashToken(resetConfirmationRequest.token());
        passwordResetRepository.findPasswordResetToken(hashedToken)
                .ifPresent(passwordResetToken -> {
                    userRepository.updatePassword(passwordResetToken.getUserId(), hashedPassword);
                    passwordResetRepository.deletePasswordResetToken(hashedToken);

                    // send confirmation email
                    sendConfirmationEmail(passwordResetToken.getUserId());
                });
    }

    private void sendConfirmationEmail(Integer userId) {
        userRepository.findUserByUserId(userId)
                .ifPresent(user -> emailService.sendPasswordResetConfirmationEmail(
                        user.getEmail(),
                        user.getUsername()));
    }

    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private void validatePasswordResetRequest(PasswordResetRequest passwordResetRequest) {
        if (passwordResetRequest.email() == null || passwordResetRequest.email().isEmpty()) {
            throw new BadCredentialsException("The Email field is required.");
        }
    }
}
