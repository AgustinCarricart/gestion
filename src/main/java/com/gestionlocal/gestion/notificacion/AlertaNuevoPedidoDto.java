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
public class AlertaNuevoPedidoDto {
    private String tipo;
    private UUID pedidoPublicId;
    private String nombreCliente;
    private String telefonoCliente;
    private LocalDateTime fechaEntrega;
    private Double total;
    private Double sena;
    private Double saldoPendiente;
    private String resumenProductos;
    private String timestamp;
}
