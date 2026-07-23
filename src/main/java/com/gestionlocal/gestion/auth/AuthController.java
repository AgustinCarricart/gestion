package com.gestionlocal.gestion.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request)
    {
        return ResponseEntity.ok(authService.login(request));
    }
    @PatchMapping("/usuarios/{publicId}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID publicId,
                                              @Valid @RequestBody ResetPasswordRequest request)
    {
        authService.resetPassword(publicId, request);
        return ResponseEntity.noContent().build();
    }
}