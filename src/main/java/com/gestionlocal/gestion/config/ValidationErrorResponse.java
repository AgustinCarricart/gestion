package com.gestionlocal.gestion.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ValidationErrorResponse
{
    private LocalDateTime timestamp;
    private int status;
    private Map<String, String> errores;
}