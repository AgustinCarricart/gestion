package com.gestionlocal.gestion.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity,Long>
{
    Optional<ProductoEntity> findByPublicId(UUID publicId);

    List<ProductoEntity> findByTipo(TipoProducto tipo);

    List<ProductoEntity> findByActivoTrue();

    boolean existsByNombre(String nombre);

    boolean existsByPublicId(UUID publicId);

}
