package com.gestionlocal.gestion;



import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoEntity;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import com.gestionlocal.gestion.pedido.PedidoService;
import com.gestionlocal.gestion.pedido.dto.DetallePedidoRequestDto;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoRequest;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoResponse;
import com.gestionlocal.gestion.pedido.exception.CantidadInvalidaException;
import com.gestionlocal.gestion.pedido.exception.EstadoTransicionInvalidaException;
import com.gestionlocal.gestion.pedido.exception.PedidoNotFoundException;
import com.gestionlocal.gestion.pedido.mapper.PedidoMapper;
import com.gestionlocal.gestion.producto.ProductoEntity;
import com.gestionlocal.gestion.producto.ProductoRepository;
import com.gestionlocal.gestion.producto.TipoProducto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
        @DisplayName("Debe retornar lista de pedidos de cocina para una fecha específica")
        void getPedidosCocinaPorFecha_Exito()
        {
            LocalDate fecha = LocalDate.now();
            when(pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
                    any(), any(), any())).thenReturn(List.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            List<PedidoDtoResponse> resultado = pedidoService.getPedidosCocinaPorFecha(fecha);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Debe retornar lista de todos los pedidos de cocina activos")
        void getPedidosCocinaActivos_Exito()
        {
            when(pedidoRepository.findByEstadoPedidoNotInOrderByFechaEntregaAsc(any()))
                    .thenReturn(List.of(pedidoEntity));
            when(pedidoMapper.toDto(pedidoEntity)).thenReturn(pedidoDtoResponse);

            List<PedidoDtoResponse> resultado = pedidoService.getPedidosCocinaActivos();

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
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

    // ── CREACIÓN DE PEDIDOS Y VALIDACIÓN DE MINIS ────────────────────────────

    @Nested
    @DisplayName("Validación de Pedidos")
    class CreacionPedidos
    {
        @Test
        @DisplayName("Debe lanzar CantidadInvalidaException si producto MINI tiene menos de 12 unidades")
        void crearPedido_ProductoMiniMenosDe12_LanzaExcepcion()
        {
            UUID prodId = UUID.randomUUID();
            ProductoEntity prodMini = new ProductoEntity();
            prodMini.setPublicId(prodId);
            prodMini.setNombreProducto("Mini pavlova");
            prodMini.setTipoProducto(TipoProducto.MINI);
            prodMini.setPrecio(3000.0);
            prodMini.setActivo(true);

            when(productoRepository.findByPublicId(prodId)).thenReturn(Optional.of(prodMini));

            DetallePedidoRequestDto detalle = new DetallePedidoRequestDto();
            detalle.setProductoPublicId(prodId);
            detalle.setCantidad(5);

            PedidoDtoRequest request = new PedidoDtoRequest();
            request.setNombreCliente("Juan Pérez");
            request.setTelefonoCliente("2231234567");
            request.setDetalles(List.of(detalle));

            CantidadInvalidaException ex = assertThrows(CantidadInvalidaException.class, () ->
                    pedidoService.createPedido(request));

            assertTrue(ex.getMessage().contains("12 unidades"));
        }

        @Test
        @DisplayName("Debe calcular precio por kilo a $45.000 para torta grande de más de 1.5 kg")
        void crearPedido_TortaGrandePorKilo_CalculaCorrectamente()
        {
            UUID prodId = UUID.randomUUID();
            ProductoEntity torta = new ProductoEntity();
            torta.setPublicId(prodId);
            torta.setNombreProducto("Torta olivia");
            torta.setTipoProducto(TipoProducto.TORTA_COMUN);
            torta.setPrecio(58000.0);
            torta.setActivo(true);

            when(productoRepository.findByPublicId(prodId)).thenReturn(Optional.of(torta));
            when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(pedidoMapper.toDto(any(PedidoEntity.class))).thenReturn(new PedidoDtoResponse());

            DetallePedidoRequestDto detalle = new DetallePedidoRequestDto();
            detalle.setProductoPublicId(prodId);
            detalle.setCantidad(1);
            detalle.setPesoKg(2.0); // 2 kg x $45.000 = $90.000

            PedidoDtoRequest request = new PedidoDtoRequest();
            request.setNombreCliente("Ana Gómez");
            request.setTelefonoCliente("2239876543");
            request.setDetalles(List.of(detalle));

            pedidoService.createPedido(request);

            verify(pedidoRepository).save(argThat(p -> {
                assertEquals(90000.0, p.getTotal());
                assertEquals(1, p.getDetalles().size());
                assertEquals(2.0, p.getDetalles().get(0).getPesoKg());
                assertEquals(90000.0, p.getDetalles().get(0).getSubtotal());
                return true;
            }));
        }

        @Test
        @DisplayName("Debe lanzar PesoInvalidoException si la torta grande no supera 1.5 kg")
        void crearPedido_TortaGrandeMenorOIgualA1KiloYMedio_LanzaExcepcion()
        {
            UUID prodId = UUID.randomUUID();
            ProductoEntity torta = new ProductoEntity();
            torta.setPublicId(prodId);
            torta.setNombreProducto("Torta olivia");
            torta.setTipoProducto(TipoProducto.TORTA_COMUN);
            torta.setPrecio(58000.0);
            torta.setActivo(true);

            when(productoRepository.findByPublicId(prodId)).thenReturn(Optional.of(torta));

            DetallePedidoRequestDto detalle = new DetallePedidoRequestDto();
            detalle.setProductoPublicId(prodId);
            detalle.setCantidad(1);
            detalle.setPesoKg(1.5); // 1.5 kg exacto (no supera el kilo y medio)

            PedidoDtoRequest request = new PedidoDtoRequest();
            request.setNombreCliente("Ana Gómez");
            request.setTelefonoCliente("2239876543");
            request.setDetalles(List.of(detalle));

            assertThrows(com.gestionlocal.gestion.pedido.exception.PesoInvalidoException.class, () ->
                    pedidoService.createPedido(request));
        }

        @Test
        @DisplayName("Debe lanzar PesoInvalidoException si la torta grande supera los 5 kg")
        void crearPedido_TortaGrandeMayorA5Kilos_LanzaExcepcion()
        {
            UUID prodId = UUID.randomUUID();
            ProductoEntity torta = new ProductoEntity();
            torta.setPublicId(prodId);
            torta.setNombreProducto("Torta olivia");
            torta.setTipoProducto(TipoProducto.TORTA_COMUN);
            torta.setPrecio(58000.0);
            torta.setActivo(true);

            when(productoRepository.findByPublicId(prodId)).thenReturn(Optional.of(torta));

            DetallePedidoRequestDto detalle = new DetallePedidoRequestDto();
            detalle.setProductoPublicId(prodId);
            detalle.setCantidad(1);
            detalle.setPesoKg(5.5); // Mayor a 5 kg

            PedidoDtoRequest request = new PedidoDtoRequest();
            request.setNombreCliente("Ana Gómez");
            request.setTelefonoCliente("2239876543");
            request.setDetalles(List.of(detalle));

            assertThrows(com.gestionlocal.gestion.pedido.exception.PesoInvalidoException.class, () ->
                    pedidoService.createPedido(request));
        }
    }
}