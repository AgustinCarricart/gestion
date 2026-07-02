package com.gestionlocal.gestion.pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity,Long>
{
    List<PedidoEntity> getByCliente_PublicId(UUID clientePublicId);

    Optional<PedidoEntity> findByPublicId(UUID publicId);

   List<PedidoEntity>  findByEstado(EstadoPedido estado);
   

}