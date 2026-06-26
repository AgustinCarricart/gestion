package com.gestionlocal.gestion.cliente.exception;

public class ClienteAlreadyExists extends RuntimeException {
    public ClienteAlreadyExists(String message) {
        super(message);
    }
}
