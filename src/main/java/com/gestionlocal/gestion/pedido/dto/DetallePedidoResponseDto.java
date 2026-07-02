package com.gestionlocal.gestion.pedido.dto;


import com.gestionlocal.gestion.producto.TipoProducto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoResponseDto
{
    private UUID productoPublicId;
    private String productoNombre;
    private TipoProducto tipoProducto;
    private Integer cantidad;
    private Double pesoKg;
    private Double precioUnitario;
    private Double subtotal;
    private String observaciones;

}
