package com.gestionlocal.gestion.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest
{
    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaPassword;
}