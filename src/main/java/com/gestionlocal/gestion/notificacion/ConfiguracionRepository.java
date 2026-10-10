package com.gestionlocal.gestion.notificacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionRepository extends JpaRepository<ConfiguracionEntity, Long> {
    Optional<ConfiguracionEntity> findByClave(String clave);
}
