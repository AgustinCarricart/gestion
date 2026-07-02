package com.gestionlocal.gestion.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "El cliente es obligatorio")
    private UUID clientePublicId;

    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDateTime fechaEntrega;

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    @Valid
    private List<DetallePedidoRequestDto> detalles;

    private String observaciones;

    private Double total;
}
