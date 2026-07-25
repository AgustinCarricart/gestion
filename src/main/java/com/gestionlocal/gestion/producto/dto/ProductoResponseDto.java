package com.gestionlocal.gestion.producto.dto;

import com.gestionlocal.gestion.producto.TipoProducto;
import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDto
{
    private UUID publicId;
    private String nombre;
    private TipoProducto tipo;
    private Double precioFijo;
    private Double precioPorKilo;
    private Double precioDocena;
    private boolean activo;
}
