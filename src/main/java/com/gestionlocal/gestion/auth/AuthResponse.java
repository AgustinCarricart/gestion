package com.gestionlocal.gestion.auth;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse
{
    private String token;
    private String rol;
    private String username;
}