package com.gestionlocal.gestion.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse
{
    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
}