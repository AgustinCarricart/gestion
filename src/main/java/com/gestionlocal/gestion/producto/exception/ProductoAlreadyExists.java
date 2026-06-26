package com.gestionlocal.gestion.producto.exception;

public class ProductoAlreadyExists extends RuntimeException {
    public ProductoAlreadyExists(String message) {
        super(message);
    }
}
