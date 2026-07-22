package com.gestionlocal.gestion.cliente;
import com.gestionlocal.gestion.cliente.dto.ClienteRequestDto;
import com.gestionlocal.gestion.cliente.dto.ClienteResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController
{
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService)
    {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> getAllClientes()
    {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponseDto>> getAllClientesActivos()
    {
        return ResponseEntity.ok(clienteService.getAllClientesActivos());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ClienteResponseDto> getClienteByPublicId(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(clienteService.getClienteByPublicId(publicId));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDto> createCliente(@Valid @RequestBody ClienteRequestDto dto)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.createCliente(dto));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<ClienteResponseDto> updateCliente(@PathVariable UUID publicId, @Valid @RequestBody ClienteRequestDto dto)
    {
        return ResponseEntity.ok(clienteService.updateCliente(publicId, dto));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteCliente(@PathVariable UUID publicId)
    {
        clienteService.deleteCliente(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}/reactivar")
    public ResponseEntity<ClienteResponseDto> reactivarCliente(@PathVariable UUID publicId)
    {
        return ResponseEntity.ok(clienteService.reactivarCliente(publicId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponseDto>> buscarClientes(
            @RequestParam String nombre,
            @RequestParam(required = false) String apellido)
    {
        return ResponseEntity.ok(clienteService.buscarClientes(nombre, apellido));
    }

}
