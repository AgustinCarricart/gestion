package com.gestionlocal.gestion.pedido;

import com.gestionlocal.gestion.cliente.ClienteEntity;
import com.gestionlocal.gestion.cliente.ClienteRepository;
import com.gestionlocal.gestion.cliente.exception.ClienteInactivoException;
import com.gestionlocal.gestion.cliente.exception.ClienteNotFoundException;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PedidoService
{
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, ProductoRepository productoRepository, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    public List<PedidoDtoResponse> getAllPedidos()
    {
        List<PedidoDtoResponse> pedidos = pedidoRepository.findAll()
                .stream()
                .map(pedidoMapper :: toDto)
                .toList();

        return pedidos;
    }

    public List<PedidoDtoResponse> getPedidosByCliente(UUID clientePublicId)
    {
        List<PedidoDtoResponse> pedidos = pedidoRepository.getByCliente_PublicId(clientePublicId)
                .stream()
                .map(pedidoMapper :: toDto)
                .toList();

        return pedidos;
    }

    public List<PedidoDtoResponse> getPedidosByEstado(EstadoPedido estado)
    {
        List<PedidoDtoResponse> pedidoDtoResponses = pedidoRepository.findAll()
                .stream()
                .filter(c -> c.getEstadoPedido().equals(estado))
                .map(pedidoMapper :: toDto)
                .toList();

        return pedidoDtoResponses;

    }

    public PedidoDtoResponse getPedidoByPublicId(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(()-> new PedidoNotFoundException("No se encontro el pedido con ese id"));
        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse createPedido(PedidoDtoRequest dto)
    {
        ClienteEntity cliente = clienteRepository.findByPublicId(dto.getClientePublicId())
                .orElseThrow(() -> new ClienteNotFoundException("No se encontró el cliente"));

        if (!cliente.isActivo())
        {
            throw new ClienteInactivoException("El cliente está inactivo");
        }

        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
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

        // Override manual si se envió
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
        // Validar primero que el tipo de producto no sea null para evitar NullPointerException
        if (producto.getTipoProducto() == null) {
            throw new IllegalArgumentException("El producto debe tener un tipo definido");
        }

        // Usamos el switch como expresión que devuelve el valor directamente
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

    @Transactional ///Borrado logico
    public PedidoDtoResponse cancelarPedido(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO)
        {
            throw new EstadoTransicionInvalidaException("No se puede cancelar un pedido ya entregado");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.NO_RETIRO)
        {
            throw new EstadoTransicionInvalidaException("El pedido ya está en estado: " + pedido.getEstado());
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse avanzarEstado(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        EstadoPedido siguiente = switch (pedido.getEstado())
        {
            case PENDIENTE -> EstadoPedido.LISTO;
            case LISTO     -> EstadoPedido.ENTREGADO;
            default -> throw new EstadoTransicionInvalidaException(
                    "No se puede avanzar desde el estado: " + pedido.getEstado());
        };

        pedido.setEstado(siguiente);

        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse cambiarEstado(UUID publicId, EstadoPedido nuevoEstado)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO ||
                pedido.getEstado() == EstadoPedido.CANCELADO)
        {
            throw new EstadoTransicionInvalidaException(
                    "No se puede modificar un pedido en estado: " + pedido.getEstado());
        }

        pedido.setEstado(nuevoEstado);

        return pedidoMapper.toDto(pedido);
    }

    @Transactional
    public PedidoDtoResponse marcarNoRetiro(UUID publicId)
    {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));

        if (pedido.getEstado() != EstadoPedido.LISTO)
        {
            throw new EstadoTransicionInvalidaException("Solo se puede marcar NO_RETIRO desde LISTO");
        }

        pedido.setEstado(EstadoPedido.NO_RETIRO);
        return pedidoMapper.toDto(pedido);
    }

}
