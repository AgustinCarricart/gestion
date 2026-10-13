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

import com.gestionlocal.gestion.pedido.dto.ReporteMensualResponseDto;
import com.gestionlocal.gestion.pedido.dto.ReporteProductoItemDto;

import com.gestionlocal.gestion.notificacion.NotificacionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PedidoService
{
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;
    private final NotificacionService notificacionService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         PedidoMapper pedidoMapper,
                         NotificacionService notificacionService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.pedidoMapper = pedidoMapper;
        this.notificacionService = notificacionService;
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
        pedido.setSena(dto.getSena() != null ? Math.max(0.0, dto.getSena()) : 0.0);

        List<DetallePedidosEntity> detalles = dto.getDetalles().stream()
                .map(d -> buildDetalle(d, pedido))
                .toList();

        pedido.setDetalles(detalles);
        pedido.setTotal(detalles.stream().mapToDouble(DetallePedidosEntity::getSubtotal).sum());

        PedidoEntity pedidoGuardado = pedidoRepository.save(pedido);

        try {
            notificacionService.notificarNuevoPedido(pedidoGuardado);
        } catch (Exception e) {
            // No interrumpir la creación del pedido si la notificación falla
        }

        return pedidoMapper.toDto(pedidoGuardado);
    }

    private DetallePedidosEntity buildDetalle(DetallePedidoRequestDto dto, PedidoEntity pedido)
    {
        ProductoEntity producto = productoRepository.findByPublicId(dto.getProductoPublicId())
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró producto con ese id"));

        if (!producto.isActivo())
        {
            throw new ProductoInactivoException("El producto no está activo");
        }

        if (esProductoMini(producto) && (dto.getCantidad() == null || dto.getCantidad() < 12))
        {
            throw new CantidadInvalidaException("Los pedidos de " + producto.getNombreProducto() + " deben ser a partir de 12 unidades (mínimo 12 u.).");
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

    private boolean esProductoMini(ProductoEntity producto) {
        if (producto == null) return false;
        String nombre = producto.getNombreProducto() != null ? producto.getNombreProducto().toLowerCase() : "";
        if (nombre.contains("mini torta")) return false;
        return producto.getTipoProducto() == TipoProducto.MINI || (nombre.startsWith("mini ") && !nombre.contains("torta"));
    }

    public static final double PRECIO_KILO_TORTA_GRANDE = 45000.0;
    public static final double PESO_MINIMO_TORTA_GRANDE = 1.5;
    public static final double PESO_MAXIMO_TORTA_GRANDE = 5.0;

    private void validarPesoTortaGrande(Double pesoKg) {
        if (pesoKg == null || pesoKg <= PESO_MINIMO_TORTA_GRANDE) {
            throw new com.gestionlocal.gestion.pedido.exception.PesoInvalidoException("Para tortas de tamaño grande por kilo, el peso debe superar el kilo y medio (mayor a 1.5 kg).");
        }
        if (pesoKg > PESO_MAXIMO_TORTA_GRANDE) {
            throw new com.gestionlocal.gestion.pedido.exception.PesoInvalidoException("El tamaño máximo permitido para tortas por kilo es de 5 kg.");
        }
    }

    private Double calcularSubtotalDetalle(ProductoEntity producto, DetallePedidoRequestDto dto) {
        if (producto.getTipoProducto() == null) {
            throw new IllegalArgumentException("El producto debe tener un tipo definido");
        }

        // Si se especificó peso en kilos: aplica la regla de tamaño grande por kilo ($45.000 / kg)
        if (dto.getPesoKg() != null && dto.getPesoKg() > 0) {
            validarPesoTortaGrande(dto.getPesoKg());
            return PRECIO_KILO_TORTA_GRANDE * dto.getPesoKg();
        }

        return switch (producto.getTipoProducto()) {

            case TORTA_COMUN, MINI, INDIVIDUAL ->
                    producto.getPrecio();

            case TORTA_POR_KILO, TORTA_POR_KILO_FRUTA -> {
                if (dto.getPesoKg() == null || dto.getPesoKg() <= 0) {
                    throw new com.gestionlocal.gestion.pedido.exception.PesoInvalidoException("El pedido por kilo debe especificar un peso válido.");
                }
                validarPesoTortaGrande(dto.getPesoKg());
                yield PRECIO_KILO_TORTA_GRANDE * dto.getPesoKg();
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

    @Transactional(readOnly = true)
    public List<PedidoDtoResponse> getPedidosCocinaHoy()
    {
        return getPedidosCocinaPorFecha(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<PedidoDtoResponse> getPedidosCocinaPorFecha(LocalDate fecha)
    {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);

        return pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
                        List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO), inicio, fin)
                .stream()
                .map(pedidoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDtoResponse> getPedidosCocinaActivos()
    {
        return pedidoRepository.findByEstadoPedidoNotInOrderByFechaEntregaAsc(
                        List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO, EstadoPedido.NO_RETIRO))
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

    @Transactional
    public PedidoDtoResponse actualizarSena(UUID publicId, Double nuevaSena) {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new PedidoNotFoundException("No se encontró pedido con ese id"));
        pedido.setSena(nuevaSena != null ? Math.max(0.0, nuevaSena) : 0.0);
        return pedidoMapper.toDto(pedido);
    }

    @Transactional(readOnly = true)
    public ReporteMensualResponseDto getReporteMensual(int anio, int mes) {
        YearMonth ym = YearMonth.of(anio, mes);
        LocalDateTime inicio = ym.atDay(1).atStartOfDay();
        LocalDateTime fin = ym.atEndOfMonth().atTime(23, 59, 59);

        List<PedidoEntity> pedidos = pedidoRepository.findByFechaEntregaBetweenOrderByFechaEntregaAsc(inicio, fin)
                .stream()
                .filter(p -> p.getEstadoPedido() != EstadoPedido.CANCELADO)
                .toList();

        double totalFacturado = 0.0;
        double totalSenas = 0.0;
        int entregados = 0;
        int pendientesOListos = 0;

        Map<String, ReporteProductoItemDto> prodMap = new HashMap<>();

        for (PedidoEntity p : pedidos) {
            double tot = p.getTotal() != null ? p.getTotal() : 0.0;
            double sen = p.getSena() != null ? p.getSena() : 0.0;
            totalFacturado += tot;
            totalSenas += sen;

            if (p.getEstadoPedido() == EstadoPedido.ENTREGADO) {
                entregados++;
            } else if (p.getEstadoPedido() == EstadoPedido.PENDIENTE || p.getEstadoPedido() == EstadoPedido.LISTO) {
                pendientesOListos++;
            }

            if (p.getDetalles() != null) {
                for (DetallePedidosEntity d : p.getDetalles()) {
                    String nombre = (d.getProducto() != null && d.getProducto().getNombreProducto() != null)
                            ? d.getProducto().getNombreProducto() : "Otro";
                    int cant = d.getCantidad() != null ? d.getCantidad() : 1;
                    double sub = d.getSubtotal() != null ? d.getSubtotal() : 0.0;
                    double kg = d.getPesoKg() != null ? d.getPesoKg() : 0.0;

                    ReporteProductoItemDto item = prodMap.computeIfAbsent(nombre, k ->
                            ReporteProductoItemDto.builder()
                                    .nombre(k)
                                    .unidades(0)
                                    .pesoKgTotal(0.0)
                                    .totalVenta(0.0)
                                    .porcentajeVentas(0.0)
                                    .build()
                    );
                    item.setUnidades(item.getUnidades() + cant);
                    item.setPesoKgTotal(item.getPesoKgTotal() + kg);
                    item.setTotalVenta(item.getTotalVenta() + sub);
                }
            }
        }

        final double finalTotal = totalFacturado;
        List<ReporteProductoItemDto> productos = prodMap.values().stream()
                .peek(item -> {
                    if (finalTotal > 0) {
                        item.setPorcentajeVentas(Math.round((item.getTotalVenta() / finalTotal * 100.0) * 10.0) / 10.0);
                    }
                })
                .sorted((a, b) -> Double.compare(b.getTotalVenta(), a.getTotalVenta()))
                .toList();

        return ReporteMensualResponseDto.builder()
                .anio(anio)
                .mes(mes)
                .totalFacturado(totalFacturado)
                .totalSenas(totalSenas)
                .totalSaldoPendiente(Math.max(0.0, totalFacturado - totalSenas))
                .totalPedidos(pedidos.size())
                .pedidosEntregados(entregados)
                .pedidosPendientesOListos(pendientesOListos)
                .productos(productos)
                .build();
    }

    @Transactional
    public void deletePedido(UUID publicId) {
        PedidoEntity pedido = pedidoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new com.gestionlocal.gestion.pedido.exception.PedidoNotFoundException("Pedido no encontrado con ID: " + publicId));
        pedidoRepository.delete(pedido);
    }

    @Transactional
    public java.util.Map<String, Object> purgarPedidosPrueba() {
        long total = pedidoRepository.count();
        try {
            pedidoRepository.truncatePedidosAndDetalles();
        } catch (Exception e) {
            pedidoRepository.deleteAll();
        }
        return java.util.Map.of(
                "status", "success",
                "message", "Todos los pedidos y registros de ventas de prueba fueron eliminados exitosamente.",
                "totalEliminados", total
        );
    }

    @Transactional
    public java.util.Map<String, Object> eliminarPedidosPorEstado(EstadoPedido estado) {
        List<PedidoEntity> pedidos = pedidoRepository.findByEstadoPedido(estado);
        long count = pedidos.size();
        pedidoRepository.deleteAll(pedidos);
        return java.util.Map.of(
                "status", "success",
                "message", "Se eliminaron " + count + " pedidos con estado " + estado + " exitosamente.",
                "totalEliminados", count
        );
    }
}
