package com.gestionlocal.gestion;

import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoEntity;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import com.gestionlocal.gestion.pedido.PedidoService;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoResponse;
import com.gestionlocal.gestion.pedido.exception.EstadoTransicionInvalidaException;
import com.gestionlocal.gestion.pedido.exception.PedidoNotFoundException;
import com.gestionlocal.gestion.pedido.mapper.PedidoMapper;
import com.gestionlocal.gestion.cliente.ClienteRepository;
import com.gestionlocal.gestion.producto.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest
{
    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @InjectMocks
    private PedidoService pedidoService;

    private PedidoEntity pedidoEntity;
    private PedidoDtoResponse pedidoDtoResponse;
    private UUID publicId;

    @BeforeEach
    void setUp()
    {
        publicId = UUID.randomUUID();

        pedidoEntity = new PedidoEntity();
        pedidoEntity.setPublicId(publicId);
        pedidoEntity.setEstadoPedido(EstadoPedido.PENDIENTE);

        pedidoDtoResponse = new PedidoDtoResponse();
    }

    // ── BUSCAR PEDIDOS ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Búsqueda de Pedidos")
    class BuscarPedidos
    {
        @Test
        @DisplayName("Debe retornar el DTO cuando el pedido existe")
        void getPedidoByPublicId_Exito()
        {
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            PedidoDtoResponse resultado = pedidoService.getPedidoByPublicId(publicId);

            assertNotNull(resultado);
            verify(pedidoRepository, times(1)).findByPublicId(publicId);
            verify(pedidoMapper, times(1)).toDto(pedidoEntity);
        }

        @Test
        @DisplayName("Debe lanzar PedidoNotFoundException cuando el publicId no existe")
        void getPedidoByPublicId_NotFound()
        {
            UUID idInexistente = UUID.randomUUID();
            when(pedidoRepository.findByPublicId(idInexistente)).thenReturn(Optional.empty());

            assertThrows(PedidoNotFoundException.class, () ->
                    pedidoService.getPedidoByPublicId(idInexistente));

            verify(pedidoRepository, times(1)).findByPublicId(idInexistente);
            verifyNoInteractions(pedidoMapper);
        }

        @Test
        @DisplayName("Debe retornar lista de pedidos por estado")
        void getPedidosByEstado_Exito()
        {
            when(pedidoRepository.findByEstadoPedido(EstadoPedido.PENDIENTE))
                    .thenReturn(List.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            List<PedidoDtoResponse> resultado = pedidoService.getPedidosByEstado(EstadoPedido.PENDIENTE);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(pedidoRepository, times(1)).findByEstadoPedido(EstadoPedido.PENDIENTE);
        }

        @Test
        @DisplayName("Debe retornar lista de pedidos por cliente")
        void getPedidosByCliente_Exito()
        {
            UUID clientePublicId = UUID.randomUUID();
            when(pedidoRepository.getByCliente_PublicId(clientePublicId))
                    .thenReturn(List.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            List<PedidoDtoResponse> resultado = pedidoService.getPedidosByCliente(clientePublicId);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(pedidoRepository, times(1)).getByCliente_PublicId(clientePublicId);
        }
    }

    // ── ESTADOS ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Transiciones de Estado")
    class TransicionesEstado
    {
        @Test
        @DisplayName("Debe avanzar de PENDIENTE a LISTO")
        void avanzarEstado_DePendienteAListo()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.PENDIENTE);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            pedidoService.avanzarEstado(publicId);

            assertEquals(EstadoPedido.LISTO, pedidoEntity.getEstadoPedido());
        }

        @Test
        @DisplayName("Debe avanzar de LISTO a ENTREGADO")
        void avanzarEstado_DeListoAEntregado()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.LISTO);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            pedidoService.avanzarEstado(publicId);

            assertEquals(EstadoPedido.ENTREGADO, pedidoEntity.getEstadoPedido());
        }

        @Test
        @DisplayName("Debe lanzar excepción al intentar avanzar desde ENTREGADO")
        void avanzarEstado_DesdeEntregado_LanzaExcepcion()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.ENTREGADO);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));

            assertThrows(EstadoTransicionInvalidaException.class, () ->
                    pedidoService.avanzarEstado(publicId));
        }

        @Test
        @DisplayName("Debe cancelar un pedido PENDIENTE")
        void cancelarPedido_DesdePendiente_Exito()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.PENDIENTE);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            pedidoService.cancelarPedido(publicId);

            assertEquals(EstadoPedido.CANCELADO, pedidoEntity.getEstadoPedido());
        }

        @Test
        @DisplayName("Debe lanzar excepción al cancelar un pedido ENTREGADO")
        void cancelarPedido_DesdeEntregado_LanzaExcepcion()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.ENTREGADO);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));

            assertThrows(EstadoTransicionInvalidaException.class, () ->
                    pedidoService.cancelarPedido(publicId));
        }

        @Test
        @DisplayName("Debe marcar NO_RETIRO desde LISTO")
        void marcarNoRetiro_DesdeListo_Exito()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.LISTO);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            pedidoService.marcarNoRetiro(publicId);

            assertEquals(EstadoPedido.NO_RETIRO, pedidoEntity.getEstadoPedido());
        }

        @Test
        @DisplayName("Debe lanzar excepción al marcar NO_RETIRO desde PENDIENTE")
        void marcarNoRetiro_DesdePendiente_LanzaExcepcion()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.PENDIENTE);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));

            assertThrows(EstadoTransicionInvalidaException.class, () ->
                    pedidoService.marcarNoRetiro(publicId));
        }

        @Test
        @DisplayName("Debe lanzar excepción al cambiar estado desde ENTREGADO")
        void cambiarEstado_DesdeEntregado_LanzaExcepcion()
        {
            pedidoEntity.setEstadoPedido(EstadoPedido.ENTREGADO);
            when(pedidoRepository.findByPublicId(publicId)).thenReturn(Optional.of(pedidoEntity));

            assertThrows(EstadoTransicionInvalidaException.class, () ->
                    pedidoService.cambiarEstado(publicId, EstadoPedido.PENDIENTE));
        }
    }
}