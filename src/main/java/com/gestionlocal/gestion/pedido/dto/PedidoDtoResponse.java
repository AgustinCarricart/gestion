package com.gestionlocal.gestion.pedido.dto;

import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.producto.TipoProducto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDtoResponse
{
    private UUID publicId;
    private UUID clientePublicId;
    private String clienteNombre;
    private String clienteApellido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;
    private EstadoPedido estado;
    private Double total;
    private String observaciones;
    private List<DetallePedidoResponseDto> detalles;

}
