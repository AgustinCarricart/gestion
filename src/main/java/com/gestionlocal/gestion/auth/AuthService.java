package com.gestionlocal.gestion.auth;

import com.gestionlocal.gestion.security.JwtService;
import com.gestionlocal.gestion.usuario.RolEnum;
import com.gestionlocal.gestion.usuario.UsuarioEntity;
import com.gestionlocal.gestion.usuario.UsuarioRepository;
import com.gestionlocal.gestion.usuario.UsuarioResponseDto;
import com.gestionlocal.gestion.usuario.exceptions.UsernameAlreadyExistsException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService
{
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder)
    {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request)
    {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getUsername(), request.getPassword())
        );

        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, usuario.getRol().name(), usuario.getUsername());
    }

    @Transactional
    public UsuarioResponseDto register(RegisterRequest request)
    {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent())
        {
            throw new UsernameAlreadyExistsException("Ya se encontro ese nombre");
        }

        if (request.getRol() == RolEnum.SUPERADMIN) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean esSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
            if (!esSuperAdmin) {
                throw new org.springframework.security.access.AccessDeniedException("Solo un SuperAdmin puede crear otros usuarios con rol SuperAdmin");
            }
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());

        usuarioRepository.save(usuario);

        return new UsuarioResponseDto(
                usuario.getPublicId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }

    public List<UsuarioResponseDto> getAllUsuarios()
    {
        return usuarioRepository.findAll()
                .stream()
                .map(u -> new UsuarioResponseDto(
                        u.getPublicId(),
                        u.getUsername(),
                        u.getRol(),
                        u.isActivo()
                ))
                .toList();
    }

    @Transactional
    public void resetPassword(UUID publicId, ResetPasswordRequest request)
    {
        UsuarioEntity usuario = usuarioRepository.findByPublicId(publicId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
    }
}