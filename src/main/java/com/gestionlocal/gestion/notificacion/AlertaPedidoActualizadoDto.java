package com.gestionlocal.gestion.notificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaPedidoActualizadoDto {
    private String tipo;
    private UUID pedidoPublicId;
    private Long pedidoId;
    private String nombreCliente;
    private String nuevoEstado;
    private LocalDateTime fechaEntrega;
    private String timestamp;
}
