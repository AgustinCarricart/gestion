package com.gestionlocal.gestion.auth;

import com.gestionlocal.gestion.security.JwtService;
import com.gestionlocal.gestion.usuario.UsuarioEntity;
import com.gestionlocal.gestion.usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService
{
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UsuarioEntity usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario);

        return new AuthResponse(token, usuario.getRol().name(), usuario.getUsername());
    }

    @Transactional
    public void resetPassword(UUID publicId, ResetPasswordRequest request)
    {
        UsuarioEntity usuario = usuarioRepository.findByPublicId(publicId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
    }

}