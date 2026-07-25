package com.gestionlocal.gestion.usuario;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDto
{
    private UUID publicId;
    private String username;
    private RolEnum rol;
    private boolean activo;
}