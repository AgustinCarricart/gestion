package com.gestionlocal.gestion.pedido;

import com.gestionlocal.gestion.pedido.dto.PedidoDtoRequest;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController
{
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService)
    {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<PedidoDtoResponse>> getPedidos(
            @RequestParam(required = false, defaultValue = "false") boolean historico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        if (historico) {
            return ResponseEntity.ok(pedidoService.getHistoricoPedidos(desde, hasta));
        }
        return ResponseEntity.ok(pedidoService.getPedidosActivos());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<PedidoDtoResponse> getPedidoByPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(pedidoService.getPedidoByPublicId(publicId));
    }


    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.getPedidosByEstado(estado));
    }

    @PostMapping
    public ResponseEntity<PedidoDtoResponse> createPedido(@Valid @RequestBody PedidoDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.createPedido(dto));
    }

    @PatchMapping("/{publicId}/avanzar")
    public ResponseEntity<PedidoDtoResponse> avanzarEstado(@PathVariable UUID publicId) {
        return ResponseEntity.ok(pedidoService.avanzarEstado(publicId));
    }

    @PatchMapping("/{publicId}/estado")
    public ResponseEntity<PedidoDtoResponse> cambiarEstado(@PathVariable UUID publicId,
                                                           @RequestParam EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(publicId, nuevoEstado));
    }

    @PatchMapping("/{publicId}/cancelar")
    public ResponseEntity<PedidoDtoResponse> cancelarPedido(@PathVariable UUID publicId) {
        return ResponseEntity.ok(pedidoService.cancelarPedido(publicId));
    }

    @PatchMapping("/{publicId}/no-retiro")
    public ResponseEntity<PedidoDtoResponse> marcarNoRetiro(@PathVariable UUID publicId) {
        return ResponseEntity.ok(pedidoService.marcarNoRetiro(publicId));
    }

    @GetMapping("/buscar-por-nombre")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByNombre(@RequestParam String nombre) {
        List<PedidoDtoResponse> pedidos = pedidoService.getPedidosByNombreCliente(nombre);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/cocina/hoy")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosCocinaHoy() {
        return ResponseEntity.ok(pedidoService.getPedidosCocinaHoy());
    }

    @GetMapping("/cocina")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosCocina(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (fecha != null) {
            return ResponseEntity.ok(pedidoService.getPedidosCocinaPorFecha(fecha));
        }
        return ResponseEntity.ok(pedidoService.getPedidosCocinaActivos());
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(pedidoService.getPedidosByFecha(fecha));
    }

    @GetMapping("/reportes/mensual")
    public ResponseEntity<com.gestionlocal.gestion.pedido.dto.ReporteMensualResponseDto> getReporteMensual(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        LocalDate hoy = LocalDate.now();
        int a = (anio != null) ? anio : hoy.getYear();
        int m = (mes != null) ? mes : hoy.getMonthValue();
        return ResponseEntity.ok(pedidoService.getReporteMensual(a, m));
    }

    @PatchMapping("/{publicId}/sena")
    public ResponseEntity<PedidoDtoResponse> actualizarSena(
            @PathVariable UUID publicId,
            @RequestBody java.util.Map<String, Double> body) {
        Double sena = body != null ? body.get("sena") : 0.0;
        return ResponseEntity.ok(pedidoService.actualizarSena(publicId, sena));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deletePedido(@PathVariable UUID publicId) {
        pedidoService.deletePedido(publicId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/purgar-pruebas")
    public ResponseEntity<java.util.Map<String, Object>> purgarPedidosPrueba() {
        return ResponseEntity.ok(pedidoService.purgarPedidosPrueba());
    }

    @DeleteMapping("/admin/purgar-entregados")
    public ResponseEntity<java.util.Map<String, Object>> purgarPedidosEntregados() {
        return ResponseEntity.ok(pedidoService.eliminarPedidosPorEstado(EstadoPedido.ENTREGADO));
    }

    @DeleteMapping("/admin/estado/{estado}")
    public ResponseEntity<java.util.Map<String, Object>> eliminarPedidosPorEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.eliminarPedidosPorEstado(estado));
    }
}