package com.horabase.api.auth;

import com.horabase.api.auth.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;
    private final PasswordService passwordService;

    public AuthController(
            AuthService authService,
            PasswordService passwordService
    ) {
        this.authService = authService;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return passwordService.changePassword(
                CurrentUser.from(jwt),
                request
        );
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return passwordService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return passwordService.resetPassword(request);
    }
}
