package com.gestionlocal.gestion.cliente;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClienteDtoRequest
{
    @NotBlank(message = "El nombre esta obligado a estar ")
    private String nombre;

    @NotBlank(message = "El apellido esta obligado a estar")
    private String apellido;

    @NotBlank(message = "El numero de telefono esta obligado a estar")
    private String numero;


}
