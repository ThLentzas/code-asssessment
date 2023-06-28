package gr.aegean.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import gr.aegean.model.auth.AuthRequest;
import gr.aegean.model.passwordreset.PasswordResetConfirmationRequest;
import gr.aegean.model.passwordreset.PasswordResetRequest;
import gr.aegean.model.passwordreset.PasswordResetResult;
import gr.aegean.model.auth.AuthResponse;
import gr.aegean.model.auth.RegisterRequest;
import gr.aegean.service.AuthService;
import gr.aegean.service.PasswordResetService;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import lombok.RequiredArgsConstructor;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * @return a ResponseEntity containing the authentication token.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(UriComponentsBuilder uriBuilder,
                                                     @Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.registerUser(request);

        URI location = uriBuilder
                .path("/api/v1/users/{userID}")
                .buildAndExpand(authResponse.getId())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(authResponse, headers, HttpStatus.CREATED);
    }

    /**
     * @return a ResponseEntity containing the authentication token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.authenticateUser(request);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/password_reset")
    public ResponseEntity<PasswordResetResult> resetPassword(
            @Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        PasswordResetResult passwordResetResult = passwordResetService.createPasswordResetToken(passwordResetRequest);

        return new ResponseEntity<>(passwordResetResult, HttpStatus.OK);
    }

    @GetMapping("/password_reset")
    public ResponseEntity<Void> resetPassword(@PathParam("token") String token) {
        passwordResetService.validatePasswordResetToken(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/password_reset/confirm")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetConfirmationRequest resetConfirmationRequest) {
        passwordResetService.resetPassword(resetConfirmationRequest);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}