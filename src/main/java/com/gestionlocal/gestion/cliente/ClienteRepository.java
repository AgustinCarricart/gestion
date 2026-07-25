package com.gestionlocal.gestion.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity,Long>
{
    Optional<ClienteEntity> findByPublicId(UUID publicId);
    Optional<ClienteEntity> findByNumero(String numero);

    boolean existsByNumero(String numero);


}
