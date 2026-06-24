package com.gestionlocal.gestion.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

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
}
