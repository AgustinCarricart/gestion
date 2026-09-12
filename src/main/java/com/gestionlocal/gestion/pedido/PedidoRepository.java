package com.gestionlocal.gestion.pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity,Long>
{
    List<PedidoEntity> getByCliente_PublicId(UUID clientePublicId);

    Optional<PedidoEntity> findByPublicId(UUID publicId);

    List<PedidoEntity> findByEstadoPedido(EstadoPedido estadoPedido);

    boolean existsByCliente_PublicIdAndEstadoPedidoIn(UUID clientePublicId, List<EstadoPedido> estados);

    boolean existsByDetalles_Producto_PublicIdAndEstadoPedidoIn(UUID productoPublicId, List<EstadoPedido> estados);

    List<PedidoEntity> findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
            List<EstadoPedido> estados, LocalDateTime inicio, LocalDateTime fin);

    List<PedidoEntity> findByFechaEntregaBetweenOrderByFechaEntregaAsc(
            LocalDateTime inicio, LocalDateTime fin);


    // Ya tenías algunos, agregás estos para el filtrado activo/histórico:

    List<PedidoEntity> findByEstadoPedidoNotInOrderByFechaEntregaAsc(List<EstadoPedido> estados);

    List<PedidoEntity> findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaDesc(
            List<EstadoPedido> estados,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    // ... mantienes el resto de tus métodos (getByCliente_PublicId, findByPublicId, etc.)
}
   

