package com.gestionlocal.gestion.pedido;

import com.gestionlocal.gestion.pedido.dto.PedidoDtoRequest;
import com.gestionlocal.gestion.pedido.dto.PedidoDtoResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<List<PedidoDtoResponse>> getAllPedidos()
    {
        return ResponseEntity.ok(pedidoService.getAllPedidos());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<PedidoDtoResponse> getPedidoByPublicId(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(pedidoService.getPedidoByPublicId(publicId));
    }

    @GetMapping("/cliente/{clientePublicId}")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByCliente(@PathVariable UUID clientePublicId)
    {
        return ResponseEntity.ok(pedidoService.getPedidosByCliente(clientePublicId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByEstado(@PathVariable EstadoPedido estado)
    {
        return ResponseEntity.ok(pedidoService.getPedidosByEstado(estado));
    }

    @PostMapping
    public ResponseEntity<PedidoDtoResponse> createPedido(@Valid @RequestBody PedidoDtoRequest dto)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.createPedido(dto));
    }

    @PatchMapping("/{publicId}/avanzar")
    public ResponseEntity<PedidoDtoResponse> avanzarEstado(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(pedidoService.avanzarEstado(publicId));
    }

    @PatchMapping("/{publicId}/estado")
    public ResponseEntity<PedidoDtoResponse> cambiarEstado(@PathVariable UUID publicId,
                                                           @RequestParam EstadoPedido nuevoEstado)
    {
        return ResponseEntity.ok(pedidoService.cambiarEstado(publicId, nuevoEstado));
    }

    @PatchMapping("/{publicId}/cancelar")
    public ResponseEntity<PedidoDtoResponse> cancelarPedido(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(pedidoService.cancelarPedido(publicId));
    }

    @PatchMapping("/{publicId}/no-retiro")
    public ResponseEntity<PedidoDtoResponse> marcarNoRetiro(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(pedidoService.marcarNoRetiro(publicId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByNombreCliente(
            @RequestParam String nombre,
            @RequestParam(required = false) String apellido)
    {
        return ResponseEntity.ok(pedidoService.getPedidosByNombreCliente(nombre, apellido));
    }

    // Empleado - pedidos del día a preparar
    @GetMapping("/cocina/hoy")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosCocinaHoy()
    {
        return ResponseEntity.ok(pedidoService.getPedidosCocinaHoy());
    }

    // Admin - buscar pedidos por día
    @GetMapping("/fecha")
    public ResponseEntity<List<PedidoDtoResponse>> getPedidosByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha)
    {
        return ResponseEntity.ok(pedidoService.getPedidosByFecha(fecha));
    }
}