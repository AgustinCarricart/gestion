package com.gestionlocal.gestion.pedido;
import org.springframework.transaction.annotation.Transactional;


import com.gestionlocal.gestion.pedido.dto.DetallePedidoRequestDto;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoRequest;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoResponse;
import com.gestionlocal.gestion.pedido.exception.CantidadInvalidaException;
import com.gestionlocal.gestion.pedido.exception.EstadoTransicionInvalidaException;
import com.gestionlocal.gestion.pedido.exception.PedidoNotFoundException;
import com.gestionlocal.gestion.pedido.exception.PesoInvalidoException;
import com.gestionlocal.gestion.pedido.mapper.PedidoMapper;
import com.gestionlocal.gestion.producto.ProductoEntity;
import com.gestionlocal.gestion.producto.ProductoRepository;
import com.gestionlocal.gestion.producto.TipoProducto;
import com.gestionlocal.gestion.producto.exception.ProductoInactivoException;
import com.gestionlocal.gestion.producto.exception.ProductoNotFoundException;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService
{
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Transactional
    public List<PedidoDtoResponse> getAllPedidos()
    {
        List<PedidoDtoResponse> pedidos = pedidoRepository.findAll()
                .stream()
                .map(pedidoMapper :: toDto)
                .toList();

        return pedidos;
    }


    @Transactional
    public List<PedidoDtoResponse> getPedidosByNombreCliente(String nombre)
    {
        return pedidoRepository.findAll()
                .stream()
                .filter(p -> p.getNombreCliente() != null && p.getNombreCliente().equalsIgnoreCase(nombre))
                .map(pedidoMapper::toDto)
                .toList();
    }

    @Transactional
    public List<PedidoDtoResponse> getPedidosByEstado(EstadoPedido estado)
    {
        List<PedidoDtoResponse> pedidoDtoResponses = pedidoRepository.findByEstadoPedido(estado)
                .stream()
                .map(pedidoMapper :: toDto)
                .toList();

        return pedidoDtoResponses;
    }

    @Transactional
    public PedidoDtoResponse getPedidoByPublicId(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(()-> new PedidoNotFoundException("No se encontro el pedido con ese id"));
        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse createPedido(PedidoDtoRequest dto)
    {
        // Validación de fecha para que sea a partir de hoy (si no se validó ya en el DTO)
        if (dto.getFechaEntrega() != null && dto.getFechaEntrega().toLocalDate().isBefore(LocalDate.now())) {
        }

        PedidoEntity pedido = new PedidoEntity();
        pedido.setNombreCliente(dto.getNombreCliente());
        pedido.setTelefonoCliente(dto.getTelefonoCliente());
        pedido.setFechaEntrega(dto.getFechaEntrega());
        pedido.setObservaciones(dto.getObservaciones());

        List<DetallePedidosEntity> detalles = dto.getDetalles().stream()
                .map(d -> buildDetalle(d, pedido))
                .toList();

        pedido.setDetalles(detalles);
        pedido.setTotal(detalles.stream().mapToDouble(DetallePedidosEntity::getSubtotal).sum());

        pedidoRepository.save(pedido);
        return pedidoMapper.toDto(pedido);
    }

    private DetallePedidosEntity buildDetalle(DetallePedidoRequestDto dto, PedidoEntity pedido)
    {
        ProductoEntity producto = productoRepository.findByPublicId(dto.getProductoPublicId())
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró producto con ese id"));

        if (!producto.isActivo())
        {
            throw new ProductoInactivoException("El producto no está activo");
        }

        Double precioUnitario = calcularSubtotalDetalle(producto, dto);

        if (dto.getPrecioUnitarioOverride() != null)
        {
            precioUnitario = dto.getPrecioUnitarioOverride();
        }

        DetallePedidosEntity detalle = new DetallePedidosEntity();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(dto.getCantidad());
        detalle.setPesoKg(dto.getPesoKg());
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(precioUnitario * dto.getCantidad());
        detalle.setObservaciones(dto.getObservaciones());

        return detalle;
    }

    private Double calcularSubtotalDetalle(ProductoEntity producto, DetallePedidoRequestDto dto) {
        if (producto.getTipoProducto() == null) {
            throw new IllegalArgumentException("El producto debe tener un tipo definido");
        }

        return switch (producto.getTipoProducto()) {

            case TORTA_COMUN, MINI, INDIVIDUAL ->
                    producto.getPrecio();

            case TORTA_POR_KILO, TORTA_POR_KILO_FRUTA -> {
                if (dto.getPesoKg() == null || dto.getPesoKg() <= 0) {
                    throw new PesoInvalidoException("El pedido por kilo debe especificar un peso válido.");
                }
                yield producto.getPrecio() * dto.getPesoKg();
            }

            case POR_DOCENA -> {
                if (dto.getCantidad() == null || dto.getCantidad() < 12) {
                    throw new CantidadInvalidaException("Para este producto, la cantidad mínima es de 12 unidades (una docena).");
                }
                yield producto.getPrecio() * dto.getCantidad();
            }
        };
    }

    @Transactional
    public PedidoDtoResponse cancelarPedido(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        if (pedido.getEstadoPedido() == EstadoPedido.ENTREGADO)
        {
            throw new EstadoTransicionInvalidaException("No se puede cancelar un pedido ya entregado");
        }
        if (pedido.getEstadoPedido() == EstadoPedido.CANCELADO || pedido.getEstadoPedido() == EstadoPedido.NO_RETIRO)
        {
            throw new EstadoTransicionInvalidaException("El pedido ya está en estado: " + pedido.getEstadoPedido());
        }

        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse avanzarEstado(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        EstadoPedido siguiente = switch (pedido.getEstadoPedido())
        {
            case PENDIENTE -> EstadoPedido.LISTO;
            case LISTO     -> EstadoPedido.ENTREGADO;
            default -> throw new EstadoTransicionInvalidaException(
                    "No se puede avanzar desde el estado: " + pedido.getEstadoPedido());
        };

        pedido.setEstadoPedido(siguiente);

        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse cambiarEstado(UUID publicId, EstadoPedido nuevoEstado)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        if (pedido.getEstadoPedido() == EstadoPedido.ENTREGADO ||
                pedido.getEstadoPedido() == EstadoPedido.CANCELADO)
        {
            throw new EstadoTransicionInvalidaException(
                    "No se puede modificar un pedido en estado: " + pedido.getEstadoPedido());
        }

        pedido.setEstadoPedido(nuevoEstado);

        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse marcarNoRetiro(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        if (pedido.getEstadoPedido() != EstadoPedido.LISTO)
        {
            throw new EstadoTransicionInvalidaException("Solo se puede marcar NO_RETIRO desde LISTO");
        }

        pedido.setEstadoPedido(EstadoPedido.NO_RETIRO);
        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public List<PedidoDtoResponse> getPedidosCocinaHoy()
    {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(23, 59, 59);

        return pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
                        List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO), inicio, fin)
                .stream()
                .map(pedidoMapper::toDto)
                .toList();
    }

    @Transactional
    public List<PedidoDtoResponse> getPedidosByFecha(LocalDate fecha)
    {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);

        return pedidoRepository.findByFechaEntregaBetweenOrderByFechaEntregaAsc(inicio, fin)
                .stream()
                .map(pedidoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDtoResponse> getPedidosActivos() {
        List<EstadoPedido> estadosFinales = List.of(
                EstadoPedido.ENTREGADO,
                EstadoPedido.CANCELADO,
                EstadoPedido.NO_RETIRO
        );
        return pedidoRepository.findByEstadoPedidoNotInOrderByFechaEntregaAsc(estadosFinales)
                .stream()
                .map(pedidoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDtoResponse> getHistoricoPedidos(LocalDateTime desde, LocalDateTime hasta) {
        List<EstadoPedido> estadosFinales = List.of(
                EstadoPedido.ENTREGADO,
                EstadoPedido.CANCELADO,
                EstadoPedido.NO_RETIRO
        );

        LocalDateTime inicio = (desde != null) ? desde : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fin = (hasta != null) ? hasta : LocalDateTime.of(2099, 12, 31, 23, 59);

        return pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaDesc(
                        estadosFinales, inicio, fin)
                .stream()
                .map(pedidoMapper::toDto)
                .toList();
    }
}
