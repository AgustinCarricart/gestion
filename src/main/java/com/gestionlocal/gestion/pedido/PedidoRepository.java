package com.gestionlocal.gestion.pedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long>
{
    @EntityGraph(attributePaths = {"detalles"})
    Optional<PedidoEntity> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByEstadoPedido(EstadoPedido estadoPedido);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByNombreClienteIgnoreCase(String nombreCliente);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByNombreClienteContainingIgnoreCase(String nombreCliente);

    // Validación de productos activos en pedidos (se mantiene por los productos, ya no por cliente)
    boolean existsByDetalles_Producto_PublicIdAndEstadoPedidoIn(UUID productoPublicId, List<EstadoPedido> estados);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
            List<EstadoPedido> estados, LocalDateTime inicio, LocalDateTime fin);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByFechaEntregaBetweenOrderByFechaEntregaAsc(
            LocalDateTime inicio, LocalDateTime fin);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByEstadoPedidoNotInOrderByFechaEntregaAsc(List<EstadoPedido> estados);

    @EntityGraph(attributePaths = {"detalles"})
    List<PedidoEntity> findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaDesc(
            List<EstadoPedido> estados,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "TRUNCATE TABLE detalle_pedido, pedidos RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncatePedidosAndDetalles();
}
   

