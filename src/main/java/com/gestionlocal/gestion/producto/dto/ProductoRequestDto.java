package com.gestionlocal.gestion.producto.dto;


import com.gestionlocal.gestion.producto.TipoProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRequestDto
{
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo es obligatorio")
    private TipoProducto tipo;

    // Solo para TORTA_COMUN
    private Double precioFijo;

    // Solo para TORTA_POR_KILO y TORTA_POR_KILO_FRUTA
    private Double precioPorKilo;

    // Solo para MINI
    private Double precioDocena;
}
