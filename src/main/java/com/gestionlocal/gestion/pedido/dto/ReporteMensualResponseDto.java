package com.gestionlocal.gestion.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReporteMensualResponseDto {
    private int anio;
    private int mes;
    private Double totalFacturado;
    private Double totalSenas;
    private Double totalSaldoPendiente;
    private int totalPedidos;
    private int pedidosEntregados;
    private int pedidosPendientesOListos;
    private List<ReporteProductoItemDto> productos;
}
