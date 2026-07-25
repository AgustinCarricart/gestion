package com.gestionlocal.gestion.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DetallePedidoRequestDto
{
    @NotNull(message = "El producto es obligatorio")
    private UUID productoPublicId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    // Requerido para TORTA_POR_KILO y TORTA_POR_KILO_FRUTA
    private Double pesoKg;

    // Si se envía, sobreescribe el precio calculado automáticamente
    private Double precioUnitarioOverride;

    private String observaciones;
}
