package com.gestionlocal.gestion.usuario;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService
{
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsServiceImpl(UsuarioRepository usuarioRepository)
    {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UsuarioEntity usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority(usuario.getRol().name()))
        );
    }
}
