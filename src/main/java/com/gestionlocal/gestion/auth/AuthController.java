package com.gestionlocal.gestion.auth;

import com.gestionlocal.gestion.usuario.UsuarioResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://frontend-gestion-dmlpw9rnn-torta-olivia.vercel.app")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health()
    {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request)
    {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDto> register(@Valid @RequestBody RegisterRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDto>> getAllUsuarios()
    {
        return ResponseEntity.ok(authService.getAllUsuarios());
    }

    @PatchMapping("/usuarios/{publicId}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID publicId,
                                              @Valid @RequestBody ResetPasswordRequest request)
    {
        authService.resetPassword(publicId, request);
        return ResponseEntity.noContent().build();
    }
}