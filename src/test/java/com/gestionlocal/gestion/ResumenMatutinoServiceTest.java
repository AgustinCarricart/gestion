package com.gestionlocal.gestion;

import com.gestionlocal.gestion.notificacion.ConfiguracionRepository;
import com.gestionlocal.gestion.notificacion.NotificacionService;
import com.gestionlocal.gestion.notificacion.ResumenMatutinoService;
import com.gestionlocal.gestion.pedido.DetallePedidosEntity;
import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoEntity;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import com.gestionlocal.gestion.producto.ProductoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumenMatutinoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ConfiguracionRepository configuracionRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private ResumenMatutinoService service;

    @Test
    @DisplayName("Arma el resumen con pedidos de hoy, lote de producción, saldo y pendientes por defecto")
    void construirMensaje_conPedidos() {
        LocalDate hoy = LocalDate.of(2026, 10, 13);

        ProductoEntity choco = new ProductoEntity();
        choco.setNombreProducto("Chocotorta");
        DetallePedidosEntity det = new DetallePedidosEntity();
        det.setProducto(choco);
        det.setCantidad(2);
        det.setPesoKg(4.5);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
        pedido.setTotal(50000.0);
        pedido.setSena(20000.0);
        pedido.setDetalles(List.of(det));

        when(pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
                any(), eq(hoy.atStartOfDay()), any())).thenReturn(List.of(pedido));
        when(pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
                any(), eq(hoy.plusDays(1).atStartOfDay()), any())).thenReturn(List.of());
        when(configuracionRepository.findByClave(anyString())).thenReturn(Optional.empty());

        String msg = service.construirMensaje(hoy);

        assertTrue(msg.contains("Buen día, Agustín"));
        assertTrue(msg.contains("Pedidos para hoy: 1"));
        assertTrue(msg.contains("$30.000"));
        assertTrue(msg.contains("2x Chocotorta (4.5 kg)"));
        assertTrue(msg.contains("Mañana: 0 pedidos agendados"));
        assertTrue(msg.contains("Cafetería MDP"));
    }

    @Test
    @DisplayName("Rechaza pendientes que superan el máximo permitido")
    void guardarPendientes_textoLargo_lanzaExcepcion() {
        String largo = "x".repeat(1001);
        assertThrows(IllegalArgumentException.class, () -> service.guardarPendientes(largo));
        verify(configuracionRepository, never()).save(any());
    }
}
