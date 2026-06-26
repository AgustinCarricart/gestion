package com.gestionlocal.gestion.cliente.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDto
{
    private UUID publicId;
    private String nombre;
    private String apellido;
    private String numero;
    private boolean activo;
}
