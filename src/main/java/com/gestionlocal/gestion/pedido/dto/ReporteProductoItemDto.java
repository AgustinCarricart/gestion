package com.gestionlocal.gestion.pedido.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReporteProductoItemDto {
    private String nombre;
    private Integer unidades;
    private Double pesoKgTotal;
    private Double totalVenta;
    private Double porcentajeVentas;
}
