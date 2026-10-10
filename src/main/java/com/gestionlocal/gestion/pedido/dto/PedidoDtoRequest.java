package com.gestionlocal.gestion.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDtoRequest
{
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    private String telefonoCliente; // Opcional o con @NotBlank si prefieres que sea obligatorio

    @NotNull(message = "La fecha de entrega es obligatoria")
    @FutureOrPresent(message = "La fecha de entrega debe ser a partir del día de hoy")
    private LocalDateTime fechaEntrega;

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    @Valid
    private List<DetallePedidoRequestDto> detalles;

    private Double sena;

    private String observaciones;
}