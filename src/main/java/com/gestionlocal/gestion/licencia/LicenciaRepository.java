package com.gestionlocal.gestion.licencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenciaRepository extends JpaRepository<LicenciaEntity, Long> {
}
