package com.gestionlocal.gestion.producto.dto;

import com.gestionlocal.gestion.producto.TipoProducto;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDto
{

    private String nombre;
    private TipoProducto tipo;
    private Integer precioFijo;
    private Integer precioPorKilo;
    private Integer precioDocena;
    private boolean activo;
}
