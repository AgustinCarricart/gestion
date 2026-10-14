package com.gestionlocal.gestion.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long>
{
    Optional<UsuarioEntity> findByUsername(String username);
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);
    Optional<UsuarioEntity> findByPublicId(UUID publicId);

}
