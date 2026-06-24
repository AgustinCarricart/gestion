package com.gestionlocal.gestion.producto.dto;

import com.gestionlocal.gestion.producto.TipoProducto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
